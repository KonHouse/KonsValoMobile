package com.example.valomobile.data.remote

import com.example.valomobile.data.remote.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface BackendApiService {

    @GET
    suspend fun getStore(
        @Url url: String,
        @Header("Authorization") token: String
    ): BackendStoreResponse

    @GET
    suspend fun getStatus(
        @Url url: String
    ): BackendStatusResponse

    @POST
    suspend fun login(
        @Url url: String,
        @Body request: LoginRequest
    ): BackendAuthResponse

    @POST
    suspend fun submit2FA(
        @Url url: String,
        @Body request: TwoFaRequest
    ): BackendAuthResponse

    @POST
    suspend fun submitTokenUrl(
        @Url url: String,
        @Body request: TokenUrlRequest
    ): BackendAuthResponse
}
