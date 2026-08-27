package com.example.valomobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class GitHubReleaseResponse(
    @SerializedName("tag_name") val tagName: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("html_url") val htmlUrl: String = "",
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("assets") val assets: List<GitHubReleaseAsset> = emptyList()
)

data class GitHubReleaseAsset(
    @SerializedName("name") val name: String = "",
    @SerializedName("browser_download_url") val browserDownloadUrl: String = "",
    @SerializedName("size") val size: Long = 0L,
    @SerializedName("content_type") val contentType: String? = null
)
