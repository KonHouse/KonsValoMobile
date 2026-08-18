package com.example.valomobile

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.valomobile.data.repository.RiotAuthRepository
import com.example.valomobile.ui.ValoApp
import com.example.valomobile.ui.theme.ValoMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var riotAuthRepository: RiotAuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAuthIntent(intent)
        enableEdgeToEdge()
        setContent {
            ValoMobileTheme {
                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle result if needed
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ValoApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkClipboardForAuth()
    }

    private fun checkClipboardForAuth() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).text?.toString() ?: ""
                if (clipText.contains("access_token=") && (clipText.contains("playvalorant.com") || clipText.contains("token="))) {
                    Log.d("MainActivity", "Found auth in clipboard, authenticating...")
                    lifecycleScope.launch {
                        riotAuthRepository.loginWithRedirectUrl(clipText)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading clipboard", e)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent?) {
        val dataString = intent?.dataString ?: return
        Log.d("MainActivity", "Received auth intent URL: $dataString")
        if (dataString.contains("playvalorant.com/opt_in") || dataString.contains("access_token=")) {
            lifecycleScope.launch {
                riotAuthRepository.loginWithRedirectUrl(dataString)
            }
        }
    }
}
