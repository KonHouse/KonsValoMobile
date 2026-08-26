package com.example.valomobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.valomobile.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudFriendsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val authRepository: RiotAuthRepository
) {
    companion object {
        private const val TAG = "CloudFriendsRepo"
        private const val PREFS_NAME = "valo_cloud_friends_prefs"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_FRIEND_CODES = "friend_codes"
        private const val COLLECTION_INVITES = "invites"
        private const val SUBCOLLECTION_FRIENDS = "friends"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private suspend fun ensureFirebaseAuth() {
        try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
                Log.d(TAG, "Firebase anonymous sign-in successful: ${auth.currentUser?.uid}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase anonymous auth warning: ${e.message}")
        }
    }

    suspend fun getMyFriendCode(): String {
        ensureFirebaseAuth()
        val puuid = authRepository.getPuuid() ?: return "VALO-0000"
        val cachedCode = prefs.getString("${puuid}_friend_code", null)
        if (!cachedCode.isNullOrBlank()) {
            return cachedCode
        }

        // Check Firestore users collection
        try {
            val userDoc = firestore.collection(COLLECTION_USERS).document(puuid).get().await()
            val existingCode = userDoc.getString("friendCode")
            if (!existingCode.isNullOrBlank()) {
                prefs.edit().putString("${puuid}_friend_code", existingCode).apply()
                return existingCode
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching friendCode from Firestore", e)
        }

        // Generate new unique code e.g. VALO-7X9K
        val newCode = generateRandomCode()
        val riotName = authRepository.getGameName() ?: "Player"
        val riotTag = authRepository.getTagLine() ?: "EU"
        val riotId = "$riotName#$riotTag"

        try {
            val codeData = mapOf(
                "puuid" to puuid,
                "riotId" to riotId,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_FRIEND_CODES).document(newCode).set(codeData).await()

            firestore.collection(COLLECTION_USERS).document(puuid).set(
                mapOf("friendCode" to newCode),
                SetOptions.merge()
            ).await()

            prefs.edit().putString("${puuid}_friend_code", newCode).apply()
            return newCode
        } catch (e: Exception) {
            Log.e(TAG, "Error saving new friend code", e)
            return newCode
        }
    }

    private fun generateRandomCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = SecureRandom()
        val suffix = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
        return "VALO-$suffix"
    }

    suspend fun syncUserProfileAndStore(
        skinOffers: List<SkinItem>,
        streakCount: Int
    ) {
        ensureFirebaseAuth()
        val puuid = authRepository.getPuuid() ?: return
        val riotName = authRepository.getGameName() ?: "Player"
        val riotTag = authRepository.getTagLine() ?: "EU"
        val riotId = "$riotName#$riotTag"
        val friendCode = getMyFriendCode()
        val region = authRepository.getRegion()

        val cloudOffers = skinOffers.take(4).map { skin ->
            mapOf(
                "uuid" to skin.uuid,
                "displayName" to skin.displayName,
                "displayIcon" to skin.displayIcon,
                "price" to skin.finalPrice,
                "tier" to skin.tier
            )
        }

        val profileData = mapOf(
            "puuid" to puuid,
            "friendCode" to friendCode,
            "riotId" to riotId,
            "region" to region,
            "currentStreak" to streakCount,
            "storeOffers" to cloudOffers,
            "lastUpdated" to System.currentTimeMillis()
        )

        try {
            firestore.collection(COLLECTION_USERS).document(puuid).set(profileData, SetOptions.merge()).await()
            // Also ensure friend_codes mapping is current
            firestore.collection(COLLECTION_FRIEND_CODES).document(friendCode).set(
                mapOf("puuid" to puuid, "riotId" to riotId),
                SetOptions.merge()
            ).await()
            Log.d(TAG, "Successfully synced user store to Firebase Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync profile to Firestore", e)
        }
    }

    suspend fun sendFriendInvite(targetInput: String): Result<String> {
        ensureFirebaseAuth()
        val myPuuid = authRepository.getPuuid() ?: return Result.failure(Exception("Not logged in to Riot"))
        val myRiotName = authRepository.getGameName() ?: "Player"
        val myRiotTag = authRepository.getTagLine() ?: "EU"
        val myRiotId = "$myRiotName#$myRiotTag"
        val myFriendCode = getMyFriendCode()

        val query = targetInput.trim()
        if (query.isBlank()) {
            return Result.failure(Exception("Please enter a Friend Code or Riot ID."))
        }

        var targetPuuid: String? = null
        var targetRiotId: String? = null

        // 1. Try search by Friend Code (e.g. VALO-XXXX or XXXX)
        val formattedCode = if (query.startsWith("VALO-", ignoreCase = true)) query.uppercase() else "VALO-${query.uppercase()}"
        try {
            val codeDoc = firestore.collection(COLLECTION_FRIEND_CODES).document(formattedCode).get().await()
            if (codeDoc.exists()) {
                targetPuuid = codeDoc.getString("puuid")
                targetRiotId = codeDoc.getString("riotId")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error looking up friend code: $formattedCode", e)
        }

        // 2. Try search by exact Riot ID (e.g. Konrad#EUNE)
        if (targetPuuid == null && query.contains("#")) {
            try {
                val userQuery = firestore.collection(COLLECTION_USERS)
                    .whereEqualTo("riotId", query)
                    .limit(1)
                    .get()
                    .await()
                if (!userQuery.isEmpty) {
                    val doc = userQuery.documents.first()
                    targetPuuid = doc.getString("puuid")
                    targetRiotId = doc.getString("riotId")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error looking up Riot ID: $query", e)
            }
        }

        if (targetPuuid.isNullOrBlank()) {
            return Result.failure(Exception("Player not found with code '$query'. Check the code and try again."))
        }

        if (targetPuuid == myPuuid) {
            return Result.failure(Exception("You cannot send a friend invite to yourself!"))
        }

        // Check if already friends
        val friendDoc = firestore.collection(COLLECTION_USERS)
            .document(myPuuid)
            .collection(SUBCOLLECTION_FRIENDS)
            .document(targetPuuid)
            .get()
            .await()

        if (friendDoc.exists()) {
            return Result.failure(Exception("You are already friends with ${targetRiotId ?: query}!"))
        }

        // Create invite document
        val inviteId = "${myPuuid}_$targetPuuid"
        val inviteData = mapOf(
            "id" to inviteId,
            "fromPuuid" to myPuuid,
            "fromRiotId" to myRiotId,
            "fromFriendCode" to myFriendCode,
            "toPuuid" to targetPuuid,
            "toRiotId" to (targetRiotId ?: "Player"),
            "status" to InviteStatus.PENDING.name,
            "createdAt" to System.currentTimeMillis()
        )

        try {
            firestore.collection(COLLECTION_INVITES).document(inviteId).set(inviteData, SetOptions.merge()).await()
            return Result.success("Invite sent to ${targetRiotId ?: formattedCode}!")
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to send invite: ${e.message}"))
        }
    }

    fun observeIncomingInvites(): Flow<List<FriendInvite>> = callbackFlow {
        ensureFirebaseAuth()
        val myPuuid = authRepository.getPuuid() ?: ""
        if (myPuuid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(COLLECTION_INVITES)
                .whereEqualTo("toPuuid", myPuuid)
                .whereEqualTo("status", InviteStatus.PENDING.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error observing invites", error)
                        return@addSnapshotListener
                    }
                    val invites = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            FriendInvite(
                                id = doc.getString("id") ?: doc.id,
                                fromPuuid = doc.getString("fromPuuid") ?: "",
                                fromRiotId = doc.getString("fromRiotId") ?: "Player",
                                fromFriendCode = doc.getString("fromFriendCode") ?: "",
                                toPuuid = doc.getString("toPuuid") ?: "",
                                toRiotId = doc.getString("toRiotId") ?: "",
                                status = doc.getString("status") ?: InviteStatus.PENDING.name,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    trySend(invites)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach invites listener", e)
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun acceptInvite(invite: FriendInvite): Result<Unit> {
        ensureFirebaseAuth()
        val myPuuid = authRepository.getPuuid() ?: return Result.failure(Exception("Not logged in"))
        try {
            val batch = firestore.batch()

            // 1. Mark invite as ACCEPTED
            val inviteRef = firestore.collection(COLLECTION_INVITES).document(invite.id)
            batch.update(inviteRef, "status", InviteStatus.ACCEPTED.name)

            // 2. Add to both friends lists
            val myFriendRef = firestore.collection(COLLECTION_USERS)
                .document(myPuuid)
                .collection(SUBCOLLECTION_FRIENDS)
                .document(invite.fromPuuid)
            batch.set(myFriendRef, mapOf("puuid" to invite.fromPuuid, "addedAt" to System.currentTimeMillis()))

            val theirFriendRef = firestore.collection(COLLECTION_USERS)
                .document(invite.fromPuuid)
                .collection(SUBCOLLECTION_FRIENDS)
                .document(myPuuid)
            batch.set(theirFriendRef, mapOf("puuid" to myPuuid, "addedAt" to System.currentTimeMillis()))

            batch.commit().await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun declineInvite(invite: FriendInvite): Result<Unit> {
        ensureFirebaseAuth()
        try {
            firestore.collection(COLLECTION_INVITES).document(invite.id).delete().await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun removeFriend(friendPuuid: String): Result<Unit> {
        ensureFirebaseAuth()
        val myPuuid = authRepository.getPuuid() ?: return Result.failure(Exception("Not logged in"))
        try {
            val batch = firestore.batch()
            val myFriendRef = firestore.collection(COLLECTION_USERS)
                .document(myPuuid)
                .collection(SUBCOLLECTION_FRIENDS)
                .document(friendPuuid)
            batch.delete(myFriendRef)

            val theirFriendRef = firestore.collection(COLLECTION_USERS)
                .document(friendPuuid)
                .collection(SUBCOLLECTION_FRIENDS)
                .document(myPuuid)
            batch.delete(theirFriendRef)

            batch.commit().await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun observeFriendsWithStores(): Flow<List<InAppFriendItem>> = callbackFlow {
        ensureFirebaseAuth()
        val myPuuid = authRepository.getPuuid() ?: ""
        if (myPuuid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var friendListener: ListenerRegistration? = null
        var userProfilesListener: ListenerRegistration? = null

        try {
            friendListener = firestore.collection(COLLECTION_USERS)
                .document(myPuuid)
                .collection(SUBCOLLECTION_FRIENDS)
                .addSnapshotListener { friendSnapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error observing friends list", error)
                        return@addSnapshotListener
                    }
                    val friendPuuids = friendSnapshot?.documents?.map { it.id } ?: emptyList()
                    if (friendPuuids.isEmpty()) {
                        trySend(emptyList())
                        userProfilesListener?.remove()
                        return@addSnapshotListener
                    }

                    userProfilesListener?.remove()
                    // Fetch up to 30 friends from users collection
                    userProfilesListener = firestore.collection(COLLECTION_USERS)
                        .whereIn("puuid", friendPuuids.take(30))
                        .addSnapshotListener { usersSnapshot, usersError ->
                            if (usersError != null) {
                                Log.w(TAG, "Error observing friend profiles", usersError)
                                return@addSnapshotListener
                            }
                            val items = usersSnapshot?.documents?.mapNotNull { doc ->
                                try {
                                    val puuid = doc.getString("puuid") ?: doc.id
                                    val friendCode = doc.getString("friendCode") ?: ""
                                    val riotId = doc.getString("riotId") ?: "Valorant Player"
                                    val region = doc.getString("region") ?: "eu"
                                    val currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0
                                    val lastUpdated = doc.getLong("lastUpdated") ?: 0L

                                    val rawOffers = doc.get("storeOffers") as? List<Map<String, Any?>> ?: emptyList()
                                    val offers = rawOffers.map { raw ->
                                        CloudStoreSkinOffer(
                                            uuid = raw["uuid"] as? String ?: "",
                                            displayName = raw["displayName"] as? String ?: "Skin",
                                            displayIcon = raw["displayIcon"] as? String,
                                            price = (raw["price"] as? Long)?.toInt() ?: ((raw["price"] as? Int) ?: 0),
                                            tierColor = raw["tierColor"] as? String ?: "#FFFFFF",
                                            tierIcon = raw["tierIcon"] as? String
                                        )
                                    }

                                    val isRecent = System.currentTimeMillis() - lastUpdated < 15 * 60 * 1000L

                                    InAppFriendItem(
                                        puuid = puuid,
                                        friendCode = friendCode,
                                        riotId = riotId,
                                        region = region,
                                        accountLevel = 0,
                                        currentStreak = currentStreak,
                                        storeOffers = offers,
                                        lastUpdated = lastUpdated,
                                        isOnlineRecently = isRecent
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            } ?: emptyList()

                            trySend(items.sortedByDescending { it.lastUpdated })
                        }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to observe friends", e)
        }

        awaitClose {
            friendListener?.remove()
            userProfilesListener?.remove()
        }
    }
}
