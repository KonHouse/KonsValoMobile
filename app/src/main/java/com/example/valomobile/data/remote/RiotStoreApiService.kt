package com.example.valomobile.data.remote

import com.example.valomobile.data.remote.model.RiotStorefrontRawResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface RiotStoreApiService {

    @POST
    suspend fun getStorefront(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Header("X-Riot-Entitlements-JWT") entitlementsToken: String,
        @Header("X-Riot-ClientVersion") clientVersion: String,
        @Header("X-Riot-ClientPlatform") clientPlatform: String,
        @Body body: Map<String, String> = emptyMap()
    ): RiotStorefrontRawResponse
}
