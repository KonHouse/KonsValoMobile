package com.example.valomobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.valomobile.data.remote.RiotAuthApiService
import com.example.valomobile.data.remote.ValorantApiService
import com.example.valomobile.data.remote.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RiotAuthResult {
    data class Success(
        val gameName: String,
        val tagLine: String,
        val region: String,
        val puuid: String
    ) : RiotAuthResult

    data class TwoFaRequired(
        val message: String,
        val email: String? = null
    ) : RiotAuthResult

    data class Error(val message: String) : RiotAuthResult
}

@Singleton
class RiotAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApiService: RiotAuthApiService,
    private val valorantApiService: ValorantApiService
) {
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences, falling back to MODE_PRIVATE", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private val cookieStore = mutableMapOf<String, String>()

    private val _sessionState = MutableStateFlow(isLoggedIn)
    val sessionState: StateFlow<Boolean> = _sessionState.asStateFlow()

    suspend fun login(username: String, password: String): RiotAuthResult = withContext(Dispatchers.IO) {
        try {
            cookieStore.clear()

            // 1. Handshake request to init session cookies
            val initRes = authApiService.authInit(
                url = "https://auth.riotgames.com/api/v1/authorization",
                userAgent = USER_AGENT,
                body = RiotAuthInitRequest()
            )
            storeCookies(initRes.headers().values("Set-Cookie"))

            // 2. Submit credentials
            val authRes = authApiService.submitCredentials(
                url = "https://auth.riotgames.com/api/v1/authorization",
                userAgent = USER_AGENT,
                cookie = getCookieHeader(),
                body = RiotAuthCredentialsRequest(username = username.trim(), password = password)
            )
            storeCookies(authRes.headers().values("Set-Cookie"))

            val body = authRes.body()
            val locationHeader = authRes.headers()["Location"]

            // Check if 2FA is required
            if (body?.type == "multifactor") {
                val email = body.multifactor?.email
                return@withContext RiotAuthResult.TwoFaRequired(
                    message = "Please enter the 6-digit security code sent to your email address.",
                    email = email
                )
            }

            // Check if login failed
            if (body?.error != null) {
                val msg = when (body.error) {
                    "auth_failure" -> "Invalid Riot account username or password."
                    "rate_limited" -> "Too many login attempts. Please wait a moment and try again."
                    else -> "Riot login error: ${body.error}"
                }
                return@withContext RiotAuthResult.Error(msg)
            }

            // Check for redirect URI containing token
            val targetUri = locationHeader 
                ?: body?.response?.parameters?.uri 
                ?: ""

            if (targetUri.isNotBlank()) {
                return@withContext finalizeFromUri(targetUri)
            }

            RiotAuthResult.Error("No authorization token received from Riot servers.")
        } catch (e: Exception) {
            Log.e(TAG, "Error in login", e)
            RiotAuthResult.Error("Connection error with Riot Games: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun submit2Fa(code: String): RiotAuthResult = withContext(Dispatchers.IO) {
        try {
            val res = authApiService.submit2FA(
                url = "https://auth.riotgames.com/api/v1/authorization",
                userAgent = USER_AGENT,
                cookie = getCookieHeader(),
                body = RiotAuth2FaRequest(code = code.trim())
            )
            storeCookies(res.headers().values("Set-Cookie"))

            val body = res.body()
            val locationHeader = res.headers()["Location"]

            if (body?.error != null) {
                return@withContext RiotAuthResult.Error("Invalid 2FA code. Please try again.")
            }

            val targetUri = locationHeader 
                ?: body?.response?.parameters?.uri 
                ?: ""

            if (targetUri.isNotBlank()) {
                return@withContext finalizeFromUri(targetUri)
            }

            RiotAuthResult.Error("Failed to verify 2FA code.")
        } catch (e: Exception) {
            Log.e(TAG, "Error in submit2Fa", e)
            RiotAuthResult.Error("2FA verification error: ${e.message}")
        }
    }

    suspend fun loginWithRedirectUrl(redirectUrl: String): RiotAuthResult = withContext(Dispatchers.IO) {
        try {
            finalizeFromUri(redirectUrl.trim())
        } catch (e: Exception) {
            RiotAuthResult.Error("Error processing login URL: ${e.message}")
        }
    }

    private suspend fun finalizeFromUri(uriString: String): RiotAuthResult {
        val decodedUri = try {
            java.net.URLDecoder.decode(uriString, "UTF-8")
        } catch (e: Exception) {
            uriString
        }

        val tokenRegex = Regex("""access_token=([A-Za-z0-9\-_=.]+)""")
        val accessTokenMatch = tokenRegex.find(decodedUri) ?: tokenRegex.find(uriString)
        val accessToken = accessTokenMatch?.groupValues?.get(1)

        val idTokenRegex = Regex("""id_token=([A-Za-z0-9\-_=.]+)""")
        val idTokenMatch = idTokenRegex.find(decodedUri) ?: idTokenRegex.find(uriString)
        val idToken = idTokenMatch?.groupValues?.get(1) ?: accessToken

        if (accessToken.isNullOrBlank()) {
            Log.e(TAG, "Failed to extract access_token from: $uriString")
            return RiotAuthResult.Error("No access_token found in the provided link. Ensure you copied the full address bar URL.")
        }

        return finalizeTokens(accessToken, idToken ?: accessToken)
    }

    suspend fun loginWithTokens(accessToken: String, idToken: String): RiotAuthResult = withContext(Dispatchers.IO) {
        try {
            finalizeTokens(accessToken.trim(), idToken.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Error in loginWithTokens", e)
            RiotAuthResult.Error("Token authentication error: ${e.message}")
        }
    }

    private suspend fun finalizeTokens(accessToken: String, idToken: String): RiotAuthResult {
        // 1. Fetch Entitlements Token
        val entitlementsRes = authApiService.getEntitlements(
            url = "https://entitlements.auth.riotgames.com/api/token/v1",
            bearerToken = "Bearer $accessToken",
            userAgent = USER_AGENT
        )
        val entitlementsToken = entitlementsRes.entitlementsToken

        // 2. Fetch UserInfo (PUUID, Riot ID, Tag)
        val userInfo = authApiService.getUserInfo(
            url = "https://auth.riotgames.com/userinfo",
            bearerToken = "Bearer $accessToken"
        )
        val puuid = userInfo.sub
        val gameName = userInfo.acct?.gameName ?: userInfo.preferredUsername ?: "Valorant"
        val tagLine = userInfo.acct?.tagLine ?: "EU"
        val region = "eu"

        // 3. Fetch latest client version
        var clientVersion = "release-13.02-shipping-17-5277781"
        try {
            val verRes = valorantApiService.getVersion()
            clientVersion = verRes.data.riotClientVersion
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch latest client version, using fallback", e)
        }

        // Save everything to EncryptedSharedPreferences
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_ID_TOKEN, idToken)
            .putString(KEY_ENTITLEMENTS_TOKEN, entitlementsToken)
            .putString(KEY_PUUID, puuid)
            .putString(KEY_REGION, region)
            .putString(KEY_GAME_NAME, gameName)
            .putString(KEY_TAG_LINE, tagLine)
            .putString(KEY_CLIENT_VERSION, clientVersion)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()

        _sessionState.value = true

        return RiotAuthResult.Success(
            gameName = gameName,
            tagLine = tagLine,
            region = region,
            puuid = puuid
        )
    }

    private fun storeCookies(setCookieHeaders: List<String>?) {
        if (setCookieHeaders == null) return
        for (header in setCookieHeaders) {
            val cookiePart = header.split(';')[0]
            val eqIdx = cookiePart.indexOf('=')
            if (eqIdx > 0) {
                val key = cookiePart.substring(0, eqIdx).trim()
                val value = cookiePart.substring(eqIdx + 1).trim()
                cookieStore[key] = value
            }
        }
    }

    private fun getCookieHeader(): String? {
        if (cookieStore.isEmpty()) return null
        return cookieStore.entries.joinToString("; ") { "${it.key}=${it.value}" }
    }

    val isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(KEY_LOGGED_IN, false) 
            && !getAccessToken().isNullOrBlank() 
            && !getEntitlementsToken().isNullOrBlank()

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    fun getEntitlementsToken(): String? = sharedPreferences.getString(KEY_ENTITLEMENTS_TOKEN, null)
    fun getPuuid(): String? = sharedPreferences.getString(KEY_PUUID, null)
    fun getRegion(): String = sharedPreferences.getString(KEY_REGION, "eu") ?: "eu"
    fun getGameName(): String? = sharedPreferences.getString(KEY_GAME_NAME, null)
    fun getTagLine(): String? = sharedPreferences.getString(KEY_TAG_LINE, null)
    fun getClientVersion(): String = sharedPreferences.getString(KEY_CLIENT_VERSION, "release-13.02-shipping-17-5277781") ?: "release-13.02-shipping-17-5277781"

    fun logout() {
        cookieStore.clear()
        sharedPreferences.edit().clear().apply()
        _sessionState.value = false
    }

    companion object {
        private const val TAG = "RiotAuthRepository"
        private const val PREFS_NAME = "riot_direct_auth_prefs"

        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_ENTITLEMENTS_TOKEN = "entitlements_token"
        private const val KEY_PUUID = "puuid"
        private const val KEY_REGION = "region"
        private const val KEY_GAME_NAME = "game_name"
        private const val KEY_TAG_LINE = "tag_line"
        private const val KEY_CLIENT_VERSION = "client_version"

        const val USER_AGENT = "RiotClient/63.0.9.4909983.4789131 rso-auth (Windows;10;;Professional, x64)"
        const val CLIENT_PLATFORM = "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9"
    }
}
