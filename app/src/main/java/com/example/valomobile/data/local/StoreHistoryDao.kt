package com.example.valomobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStoreHistory(history: StoreHistoryEntity)

    @Query("SELECT * FROM store_history WHERE date = :date AND puuid = :puuid LIMIT 1")
    suspend fun getStoreHistoryForDate(date: String, puuid: String): StoreHistoryEntity?

    @Query("SELECT * FROM store_history WHERE puuid = :puuid")
    fun getAllStoreHistory(puuid: String): Flow<List<StoreHistoryEntity>>

    @Query("SELECT DISTINCT date FROM store_history WHERE puuid = :puuid")
    fun getRecordedDates(puuid: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM store_history WHERE puuid = :puuid")
    suspend fun getRecordedCount(puuid: String): Int
}
