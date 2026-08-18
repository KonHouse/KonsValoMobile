package com.example.valomobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Bundle(
    val uuid: String,
    val displayName: String,
    val displayIcon: String,
    val description: String,
    val price: Int,
    val items: List<SkinItem>
)
