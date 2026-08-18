package com.example.valomobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey
    val uuid: String,
    val name: String,
    val displayIcon: String,
    val price: Int,
    val tier: String = "Select"
)
