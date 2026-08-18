package com.example.valomobile.data.remote.model

import com.google.gson.annotations.SerializedName

// --- RSO Auth Request Models ---

data class RiotAuthInitRequest(
    @SerializedName("client_id") val clientId: String = "play-valorant-web-prod",
    @SerializedName("response_type") val responseType: String = "token id_token",
    @SerializedName("redirect_uri") val redirectUri: String = "https://playvalorant.com/opt_in",
    @SerializedName("scope") val scope: String = "account openid",
    @SerializedName("nonce") val nonce: String = "1"
)

data class RiotAuthCredentialsRequest(
    @SerializedName("type") val type: String = "auth",
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("remember") val remember: Boolean = true
)

data class RiotAuth2FaRequest(
    @SerializedName("type") val type: String = "multifactor",
    @SerializedName("code") val code: String,
    @SerializedName("rememberDevice") val rememberDevice: Boolean = true
)

// --- RSO Auth Response Models ---

data class RiotAuthResponse(
    @SerializedName("type") val type: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("response") val response: RiotAuthResponseData? = null,
    @SerializedName("multifactor") val multifactor: RiotMultifactorData? = null
)

data class RiotAuthResponseData(
    @SerializedName("mode") val mode: String? = null,
    @SerializedName("parameters") val parameters: RiotAuthParameters? = null
)

data class RiotAuthParameters(
    @SerializedName("uri") val uri: String? = null
)

data class RiotMultifactorData(
    @SerializedName("email") val email: String? = null,
    @SerializedName("method") val method: String? = null,
    @SerializedName("methods") val methods: List<String>? = null
)

// --- Entitlements & UserInfo Models ---

data class RiotEntitlementsResponse(
    @SerializedName("entitlements_token") val entitlementsToken: String
)

data class RiotUserInfoResponse(
    @SerializedName("sub") val sub: String, // PUUID
    @SerializedName("acct") val acct: RiotAcctInfo? = null,
    @SerializedName("preferred_username") val preferredUsername: String? = null
)

data class RiotAcctInfo(
    @SerializedName("game_name") val gameName: String? = null,
    @SerializedName("tag_line") val tagLine: String? = null
)

data class ValorantVersionResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: ValorantVersionData
)

data class ValorantVersionData(
    @SerializedName("manifestId") val manifestId: String,
    @SerializedName("riotClientVersion") val riotClientVersion: String,
    @SerializedName("version") val version: String
)
