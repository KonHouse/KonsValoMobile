package com.example.valomobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.valomobile.data.remote.GitHubApiService
import com.example.valomobile.data.remote.model.GitHubReleaseAsset
import com.example.valomobile.data.remote.model.GitHubReleaseResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import retrofit2.Response

class AppUpdateRepositoryTest {

    private lateinit var context: Context
    private lateinit var gitHubApiService: GitHubApiService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val memoryStore = mutableMapOf<String, Any>()
    private lateinit var repository: AppUpdateRepository

    @Before
    fun setUp() {
        memoryStore.clear()
        context = mock(Context::class.java)
        gitHubApiService = mock(GitHubApiService::class.java)
        sharedPreferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)

        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val value = invocation.getArgument<String>(1)
            memoryStore[key] = value
            editor
        }
        `when`(sharedPreferences.getString(anyString(), any())).thenAnswer { invocation ->
            val key = invocation.getArgument<String>(0)
            val def = invocation.getArgument<String?>(1)
            (memoryStore[key] as? String) ?: def
        }

        repository = AppUpdateRepository(context, gitHubApiService)
    }

    @Test
    fun isNewerVersion_correctlyComparesSemanticVersions() {
        assertTrue(repository.isNewerVersion("1.4.2", "1.4.1"))
        assertTrue(repository.isNewerVersion("1.5.0", "1.4.9"))
        assertTrue(repository.isNewerVersion("2.0.0", "1.9.9"))
        assertTrue(repository.isNewerVersion("1.4.10", "1.4.9"))

        assertFalse(repository.isNewerVersion("1.4.1", "1.4.1"))
        assertFalse(repository.isNewerVersion("1.4.0", "1.4.1"))
        assertFalse(repository.isNewerVersion("1.3.9", "1.4.0"))
        assertFalse(repository.isNewerVersion("", "1.4.0"))
    }

    @Test
    fun checkForUpdate_whenNewerReleaseExists_returnsAppUpdateInfo() = runBlocking {
        val mockResponse = GitHubReleaseResponse(
            tagName = "v1.4.2",
            name = "ValoMobile v1.4.2",
            body = "Added GitHub update checker!",
            htmlUrl = "https://github.com/KonHouse/KonsValoMobile/releases/tag/v1.4.2",
            assets = listOf(
                GitHubReleaseAsset(
                    name = "ValoMobile-v1.4.2.apk",
                    browserDownloadUrl = "https://github.com/KonHouse/KonsValoMobile/releases/download/v1.4.2/ValoMobile-v1.4.2.apk"
                )
            )
        )

        `when`(gitHubApiService.getLatestRelease(anyString(), anyString()))
            .thenReturn(Response.success(mockResponse))

        val result = repository.checkForUpdate(currentVersion = "1.4.1")

        assertTrue(result.isSuccess)
        val update = result.getOrNull()
        assertNotNull(update)
        assertEquals("v1.4.2", update?.latestVersion)
        assertEquals("https://github.com/KonHouse/KonsValoMobile/releases/download/v1.4.2/ValoMobile-v1.4.2.apk", update?.downloadUrl)
    }

    @Test
    fun checkForUpdate_whenSameVersion_returnsNull() = runBlocking {
        val mockResponse = GitHubReleaseResponse(
            tagName = "v1.4.1",
            name = "ValoMobile v1.4.1",
            htmlUrl = "https://github.com/KonHouse/KonsValoMobile/releases/tag/v1.4.1"
        )

        `when`(gitHubApiService.getLatestRelease(anyString(), anyString()))
            .thenReturn(Response.success(mockResponse))

        val result = repository.checkForUpdate(currentVersion = "1.4.1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun dismissUpdate_persistsDismissedVersion() {
        assertFalse(repository.isVersionDismissed("v1.4.2"))
        repository.dismissUpdate("v1.4.2")
        assertTrue(repository.isVersionDismissed("v1.4.2"))
    }
}
