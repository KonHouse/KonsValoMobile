package com.example.valomobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WishlistEntity::class, StoreHistoryEntity::class], version = 3, exportSchema = false)
abstract class ValoDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
    abstract fun storeHistoryDao(): StoreHistoryDao
}
