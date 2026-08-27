package com.example.valomobile.data.remote

import com.example.valomobile.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface RiotAuthApiService {

    @POST
    suspend fun authInit(
        @Url url: String,
        @Header("User-Agent") userAgent: String,
        @Header("Accept") accept: String = "application/json",
        @Body body: RiotAuthInitRequest
    ): Response<RiotAuthResponse>

    @PUT
    suspend fun submitCredentials(
        @Url url: String,
        @Header("User-Agent") userAgent: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Cookie") cookie: String?,
        @Body body: RiotAuthCredentialsRequest
    ): Response<RiotAuthResponse>

    @PUT
    suspend fun submit2FA(
        @Url url: String,
        @Header("User-Agent") userAgent: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Cookie") cookie: String?,
        @Body body: RiotAuth2FaRequest
    ): Response<RiotAuthResponse>

    @POST
    suspend fun getEntitlements(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Header("User-Agent") userAgent: String,
        @Body body: Map<String, String> = emptyMap()
    ): RiotEntitlementsResponse

    @GET
    suspend fun getUserInfo(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): RiotUserInfoResponse

    @PUT
    suspend fun getPasRegion(
        @Url url: String = "https://riot-geo.pas.si.riotgames.com/pas/v1/product/valorant",
        @Header("Authorization") bearerToken: String,
        @Body body: Map<String, String>
    ): Response<okhttp3.ResponseBody>
}
