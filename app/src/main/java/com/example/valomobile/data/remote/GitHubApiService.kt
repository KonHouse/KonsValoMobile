package com.example.valomobile.data.remote

import com.example.valomobile.data.remote.model.GitHubReleaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface GitHubApiService {

    @Headers("Accept: application/vnd.github+json", "User-Agent: ValoMobile-Android-App")
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String = "KonHouse",
        @Path("repo") repo: String = "KonsValoMobile"
    ): Response<GitHubReleaseResponse>
}
