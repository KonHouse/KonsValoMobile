package com.example.valomobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.valomobile.data.remote.RiotAuthApiService
import com.example.valomobile.data.remote.ValorantApiService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

class RiotAuthRepositoryTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var authApiService: RiotAuthApiService
    private lateinit var valorantApiService: ValorantApiService
    private lateinit var repository: RiotAuthRepository

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        sharedPreferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)
        authApiService = mock(RiotAuthApiService::class.java)
        valorantApiService = mock(ValorantApiService::class.java)

        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor)

        repository = RiotAuthRepository(context, authApiService, valorantApiService)
    }

    @Test
    fun testIsLoggedIn_falseWhenNoTokens() {
        `when`(sharedPreferences.getBoolean(eq("logged_in"), eq(false))).thenReturn(false)
        `when`(sharedPreferences.getString(eq("access_token"), isNull())).thenReturn(null)
        `when`(sharedPreferences.getString(eq("entitlements_token"), isNull())).thenReturn(null)

        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun testIsLoggedIn_trueWhenTokensPresent() {
        `when`(sharedPreferences.getBoolean(eq("logged_in"), eq(false))).thenReturn(true)
        `when`(sharedPreferences.getString(eq("access_token"), isNull())).thenReturn("mock_access_token")
        `when`(sharedPreferences.getString(eq("entitlements_token"), isNull())).thenReturn("mock_entitlements_token")

        assertTrue(repository.isLoggedIn)
    }
}
