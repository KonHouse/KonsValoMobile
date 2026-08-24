package com.example.valomobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class ValorantApiResponse<T>(
    val status: Int,
    val data: T
)

data class ValorantSkin(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?,
    val contentTierUuid: String?,
    val chromas: List<ValorantChroma> = emptyList(),
    val levels: List<ValorantLevel> = emptyList()
)

data class ValorantChroma(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?,
    val fullRender: String?,
    val swatches: String?,
    val streamedVideo: String?,
    val assetPath: String?
)

data class ValorantLevel(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?,
    val streamedVideo: String?,
    val assetPath: String?
)

data class ValorantContentTier(
    val uuid: String,
    val devName: String?,
    val displayName: String,
    val highlightColor: String?,
    val displayIcon: String?
)

data class ValorantBundleItem(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?,
    val description: String?,
    val verticalPromoImage: String?
)

data class ValorantPlayerCard(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?,
    val smallArt: String?,
    val wideArt: String?,
    val largeArt: String?
)

data class ValorantBuddy(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?,
    val levels: List<ValorantBuddyLevel> = emptyList()
)

data class ValorantBuddyLevel(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?
)

data class ValorantSpray(
    val uuid: String,
    val displayName: String,
    val displayIcon: String?,
    val fullTransparentIcon: String?,
    val animationPng: String?,
    val animationGif: String?
)
