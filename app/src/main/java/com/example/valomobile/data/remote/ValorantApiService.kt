package com.example.valomobile.data.remote

import com.example.valomobile.data.remote.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ValorantApiService {

    @GET("v1/weapons/skins")
    suspend fun getSkins(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantSkin>>

    @GET("v1/weapons/skins/{uuid}")
    suspend fun getSkinByUuid(
        @Path("uuid") uuid: String,
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<ValorantSkin>

    @GET("v1/playercards")
    suspend fun getPlayerCards(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantPlayerCard>>

    @GET("v1/buddies")
    suspend fun getBuddies(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantBuddy>>

    @GET("v1/sprays")
    suspend fun getSprays(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantSpray>>

    @GET("v1/contenttiers")
    suspend fun getContentTiers(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantContentTier>>

    @GET("v1/bundles")
    suspend fun getBundles(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantBundleItem>>

    @GET("v1/competitivetiers")
    suspend fun getCompetitiveTiers(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantCompetitiveTierEpisode>>

    @GET("v1/maps")
    suspend fun getMaps(
        @Query("language") language: String = "en-US"
    ): ValorantApiResponse<List<ValorantMap>>

    @GET("v1/version")
    suspend fun getVersion(): ValorantVersionResponse
}
