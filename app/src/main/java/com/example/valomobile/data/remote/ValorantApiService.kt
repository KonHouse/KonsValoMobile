package com.example.valomobile.data.remote

import com.example.valomobile.data.remote.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface ValorantApiService {

    @GET("v1/weapons/skins")
    suspend fun getSkins(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantSkin>>

    @GET("v1/contenttiers")
    suspend fun getContentTiers(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantContentTier>>

    @GET("v1/bundles")
    suspend fun getBundles(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantBundleItem>>

    @GET("v1/version")
    suspend fun getVersion(): ValorantVersionResponse
}
