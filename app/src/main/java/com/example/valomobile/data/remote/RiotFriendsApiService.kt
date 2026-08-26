package com.example.valomobile.data.remote

import com.example.valomobile.data.remote.model.RiotFriendsResponse
import com.example.valomobile.data.remote.model.RiotNameServicePlayer
import com.example.valomobile.data.remote.model.RiotPresencesResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface RiotFriendsApiService {

    @GET
    suspend fun getPasChatToken(
        @Url url: String = "https://riot-geo.pas.si.riotgames.com/pas/v1/service/chat",
        @Header("Authorization") authHeader: String
    ): Response<ResponseBody>

    @GET
    suspend fun getRawGet(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Header("X-Riot-Entitlements-JWT") entitlementsToken: String? = null,
        @Header("X-Riot-ClientVersion") clientVersion: String? = null,
        @Header("X-Riot-ClientPlatform") clientPlatform: String? = null
    ): Response<ResponseBody>

    @PUT
    suspend fun getRawPut(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Header("X-Riot-Entitlements-JWT") entitlementsToken: String? = null,
        @Header("X-Riot-ClientVersion") clientVersion: String? = null,
        @Header("X-Riot-ClientPlatform") clientPlatform: String? = null,
        @Body body: List<String>
    ): Response<ResponseBody>

    @GET
    suspend fun getPresences(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Header("X-Riot-Entitlements-JWT") entitlementsToken: String,
        @Header("X-Riot-ClientVersion") clientVersion: String,
        @Header("X-Riot-ClientPlatform") clientPlatform: String
    ): RiotPresencesResponse

    @GET
    suspend fun getFriends(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Header("X-Riot-Entitlements-JWT") entitlementsToken: String,
        @Header("X-Riot-ClientVersion") clientVersion: String,
        @Header("X-Riot-ClientPlatform") clientPlatform: String
    ): RiotFriendsResponse

    @PUT
    suspend fun getPlayerNames(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Header("X-Riot-Entitlements-JWT") entitlementsToken: String,
        @Header("X-Riot-ClientVersion") clientVersion: String,
        @Header("X-Riot-ClientPlatform") clientPlatform: String,
        @Body puuids: List<String>
    ): List<RiotNameServicePlayer>
}
