package com.example.valomobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WishlistEntity::class], version = 2, exportSchema = false)
abstract class ValoDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
}
