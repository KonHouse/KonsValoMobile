package com.example.valomobile.ui.screens.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val RIOT_AUTH_URL =
    "https://auth.riotgames.com/authorize?client_id=play-valorant-web-prod&response_type=token%20id_token&redirect_uri=https%3A%2F%2Fplayvalorant.com%2Fopt_in&scope=account%20openid&nonce=1"

private const val RIOT_ACCOUNT_URL = "https://account.riotgames.com/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiotLoginScreen(
    viewModel: RiotLoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAutoredirectDialog by remember { mutableStateOf(false) }

    val webViewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accessToken = result.data?.getStringExtra(RiotLoginActivity.EXTRA_ACCESS_TOKEN) ?: ""
            val idToken = result.data?.getStringExtra(RiotLoginActivity.EXTRA_ID_TOKEN) ?: accessToken
            val cookies = result.data?.getStringExtra(RiotLoginActivity.EXTRA_COOKIES)
            if (accessToken.isNotBlank()) {
                viewModel.loginWithTokens(accessToken, idToken, cookies)
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Header: App Logo & Title
                Image(
                    painter = painterResource(id = com.example.valomobile.R.drawable.valo_icon),
                    contentDescription = "Valorant Icon",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VALOMOBILE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Preview of your Valorant store",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Push Main Button into exact vertical center
                Spacer(modifier = Modifier.weight(1f))

                // CENTER: Futuristic Cyber LED Action Tile
                Box(
                    modifier = Modifier
                        .size(width = 250.dp, height = 150.dp)
                        .shadow(
                            elevation = 22.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = Color(0xFFFF4655),
                            ambientColor = Color(0xFFFF4655)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF4655),
                                    Color(0xFF8B1221),
                                    Color(0xFFFF4655)
                                )
                            )
                        )
                        .padding(2.dp) // LED Neon Border Stroke
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E2836),
                                    Color(0xFF0F1923),
                                    Color(0xFF0A0F15)
                                )
                            )
                        )
                        .clickable(enabled = uiState !is LoginState.Loading) {
                            val intent = Intent(context, RiotLoginActivity::class.java)
                            webViewLauncher.launch(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle glowing ambient light behind icon
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF4655).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        // Glowing LED Icon Container
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    spotColor = Color(0xFFFF4655),
                                    ambientColor = Color(0xFFFF4655)
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFFFF4655),
                                            Color(0xFFFF1E38)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(34.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "CHECK YOUR SHOP",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Error Card (if any error occurs)
                AnimatedVisibility(
                    visible = uiState is LoginState.Error,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val errorMsg = (uiState as? LoginState.Error)?.message ?: ""
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                // Loading Indicator
                AnimatedVisibility(
                    visible = uiState is LoginState.Loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val loadingMsg = (uiState as? LoginState.Loading)?.message ?: "Opening store..."
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = loadingMsg,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Push Remaining Action Buttons to bottom
                Spacer(modifier = Modifier.weight(1f))

                // BOTTOM: Secondary Translucent Glass Actions (In the Shadow)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Enable Autoredirect Translucent Glass Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color.Black.copy(alpha = 0.4f),
                                ambientColor = Color.Black.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF1A2432).copy(alpha = 0.55f),
                                        Color(0xFF121A24).copy(alpha = 0.55f)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFF2E3E52).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .clickable { showAutoredirectDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = Color(0xFFFFD700).copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Autoredirect",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Riot Games Account Translucent Glass Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color.Black.copy(alpha = 0.4f),
                                ambientColor = Color.Black.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF161E28).copy(alpha = 0.45f),
                                        Color(0xFF0F151C).copy(alpha = 0.45f)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFF263342).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val customTabsIntent = CustomTabsIntent.Builder()
                                        .setShowTitle(true)
                                        .setDefaultColorSchemeParams(
                                            CustomTabColorSchemeParams.Builder()
                                                .setToolbarColor(AndroidColor.parseColor("#0F1923"))
                                                .build()
                                        )
                                        .build()
                                    customTabsIntent.launchUrl(context, Uri.parse(RIOT_ACCOUNT_URL))
                                } catch (e: Exception) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RIOT_ACCOUNT_URL))
                                    context.startActivity(intent)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.ManageAccounts,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Riot Games Account (Log out / Switch)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Autoredirect Explanatory Dialog
            if (showAutoredirectDialog) {
                AlertDialog(
                    onDismissRequest = { showAutoredirectDialog = false },
                    icon = {
                        Icon(
                            Icons.Rounded.FlashOn, 
                            contentDescription = null, 
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Enable 1-Click Autoredirect",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "To open your store instantly with 1-click without manually copying links, Android requires permission to open supported web addresses.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "How to enable in Android settings:",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "1. Tap 'Open Settings' below.\n2. Tap 'Open by default' or 'Set as default'.\n3. Tap 'Add link' and enable: playvalorant.com",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showAutoredirectDialog = false
                                try {
                                    val intent = Intent(
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
                                        } else {
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        },
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF4655)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Open Settings", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAutoredirectDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
