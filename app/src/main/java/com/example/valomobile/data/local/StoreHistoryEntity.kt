package com.example.valomobile.data.local

import androidx.room.Entity

@Entity(tableName = "store_history", primaryKeys = ["date", "puuid"])
data class StoreHistoryEntity(
    val date: String, // Format: YYYY-MM-DD
    val puuid: String,
    val itemsJson: String, // Serialized List<SkinItem>
    val timestamp: Long = System.currentTimeMillis()
)
