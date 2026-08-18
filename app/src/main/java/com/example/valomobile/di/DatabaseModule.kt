package com.example.valomobile.di

import android.content.Context
import androidx.room.Room
import com.example.valomobile.data.local.ValoDatabase
import com.example.valomobile.data.local.WishlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideValoDatabase(@ApplicationContext context: Context): ValoDatabase {
        return Room.databaseBuilder(
            context,
            ValoDatabase::class.java,
            "valo_database"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideWishlistDao(database: ValoDatabase): WishlistDao {
        return database.wishlistDao()
    }
}
