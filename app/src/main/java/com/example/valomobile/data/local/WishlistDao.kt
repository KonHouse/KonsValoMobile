package com.example.valomobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist")
    fun getAllWishlistItems(): Flow<List<WishlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE uuid = :uuid")
    suspend fun deleteByUuid(uuid: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE uuid = :uuid)")
    fun isInWishlist(uuid: String): Boolean

    @Query("SELECT * FROM wishlist")
    suspend fun getAllWishlistItemsSync(): List<WishlistEntity>
}
