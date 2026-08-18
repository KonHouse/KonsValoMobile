package com.example.valomobile.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WishlistDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ValoDatabase
    private lateinit var wishlistDao: WishlistDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ValoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        wishlistDao = database.wishlistDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val item = WishlistEntity("1", "Vandal", "image_url", 2900)
        wishlistDao.insert(item)

        val allItems = wishlistDao.getAllWishlistItems().first()
        assertEquals(1, allItems.size)
        assertEquals("Vandal", allItems[0].name)
    }

    @Test
    fun deleteItem() = runBlocking {
        val item = WishlistEntity("2", "Phantom", "image_url", 2900)
        wishlistDao.insert(item)
        wishlistDao.deleteByUuid("2")

        val allItems = wishlistDao.getAllWishlistItems().first()
        assertTrue(allItems.isEmpty())
    }

    @Test
    fun isInWishlist() = runBlocking {
        val item = WishlistEntity("3", "Operator", "image_url", 4700)
        wishlistDao.insert(item)

        assertTrue(wishlistDao.isInWishlist("3"))
        assertFalse(wishlistDao.isInWishlist("4"))
    }
}
