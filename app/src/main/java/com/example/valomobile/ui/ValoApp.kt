package com.example.valomobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.data.remote.model.UserWallet
import com.example.valomobile.ui.navigation.ValoNavKey
import com.example.valomobile.ui.screens.calculator.VpCalculatorScreen
import com.example.valomobile.ui.screens.catalog.CatalogScreen
import com.example.valomobile.ui.screens.catalog.CatalogViewModel
import com.example.valomobile.ui.screens.catalog.WishlistScreen
import com.example.valomobile.ui.screens.login.RiotLoginScreen
import com.example.valomobile.ui.screens.login.RiotLoginViewModel
import com.example.valomobile.ui.screens.settings.SettingsScreen
import com.example.valomobile.ui.screens.settings.SettingsViewModel
import com.example.valomobile.ui.screens.friends.FriendsScreen
import com.example.valomobile.domain.model.DailyStreakInfo
import com.example.valomobile.ui.components.DailyStreakCelebrationDialog
import com.example.valomobile.ui.components.DailyStreakDetailDialog
import com.example.valomobile.ui.components.StoreHistoryCalendarDialog
import com.example.valomobile.ui.components.StreakChip
import com.example.valomobile.ui.screens.store.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ValoApp() {
    val loginViewModel: RiotLoginViewModel = hiltViewModel()
    val storeViewModel: StoreViewModel = hiltViewModel()
    val isLoggedIn = loginViewModel.isLoggedIn()
    val wallet by storeViewModel.wallet.collectAsState()
    val streakInfo by storeViewModel.streakInfo.collectAsState()
    val celebrationEvent by storeViewModel.celebrationEvent.collectAsState()
    val recordedDates by storeViewModel.getRecordedDates().collectAsState(initial = emptySet())
    var showStreakDetailDialog by remember { mutableStateOf(false) }
    var showCalendarHistoryDialog by remember { mutableStateOf(false) }
    
    val backStack = rememberNavBackStack(
        if (isLoggedIn) ValoNavKey.StoreRotation as NavKey else ValoNavKey.Connect as NavKey
    )
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    
    // Auto-refresh store and balances when app returns to foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (storeViewModel.isLoggedIn) {
                    storeViewModel.checkDailyStreak()
                    storeViewModel.loadData(silent = true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    val currentKey = backStack.last()
    val showNavigation = currentKey !is ValoNavKey.Connect && currentKey !is ValoNavKey.Settings && currentKey !is ValoNavKey.VpCalculator && currentKey !is ValoNavKey.Friends
    val showTopBar = currentKey !is ValoNavKey.Connect && currentKey !is ValoNavKey.Settings && currentKey !is ValoNavKey.VpCalculator && currentKey !is ValoNavKey.Friends
    
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val useRail = showNavigation && adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    // Daily Streak Celebration Dialog
    celebrationEvent?.let { event ->
        DailyStreakCelebrationDialog(
            streak = event.currentStreak,
            wasBroken = event.wasBroken,
            onDismiss = { storeViewModel.dismissCelebration() }
        )
    }

    // Daily Streak Detail Modal
    if (showStreakDetailDialog) {
        DailyStreakDetailDialog(
            streakInfo = streakInfo,
            onViewFullHistory = {
                showStreakDetailDialog = false
                showCalendarHistoryDialog = true
            },
            onDismiss = { showStreakDetailDialog = false }
        )
    }

    // Full Store History Calendar Dialog
    if (showCalendarHistoryDialog) {
        StoreHistoryCalendarDialog(
            recordedDates = recordedDates,
            onLoadHistoryForDate = { date ->
                storeViewModel.getStoreHistoryForDate(date)
            },
            onDismiss = { showCalendarHistoryDialog = false }
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (useRail) {
            ValoNavigationRail(
                currentKey = currentKey,
                backStack = backStack
            )
        }
        
        Scaffold(
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = {
                            if (showNavigation) {
                                StreakChip(
                                    streakCount = streakInfo.currentStreak,
                                    onClick = { showStreakDetailDialog = true },
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        },
                        actions = {
                            if (showNavigation) {
                                WalletBadgeBar(
                                    wallet = wallet,
                                    onVpClick = {
                                        if (currentKey !is ValoNavKey.VpCalculator) {
                                            backStack.add(ValoNavKey.VpCalculator)
                                        }
                                    },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                            IconButton(onClick = { backStack.add(ValoNavKey.Friends) }) {
                                Icon(Icons.Rounded.People, contentDescription = "Friends")
                            }
                            IconButton(onClick = { backStack.add(ValoNavKey.Settings) }) {
                                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showNavigation && !useRail) {
                    ValoNavigationBar(
                        currentKey = currentKey,
                        backStack = backStack
                    )
                }
            }
        ) { paddingValues ->
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                sceneStrategies = listOf(listDetailStrategy),
                entryProvider = entryProvider {
                    entry<ValoNavKey.Connect> {
                        RiotLoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                backStack.removeLastOrNull()
                                backStack.add(ValoNavKey.StoreRotation)
                            }
                        )
                    }

                    entry<ValoNavKey.StoreRotation>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { StoreDetailPlaceholder("Select a skin from the store") }
                        )
                    ) {
                        StoreRotationScreen(
                            viewModel = storeViewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            },
                            onNavigateToConnect = {
                                loginViewModel.resetState()
                                while (backStack.isNotEmpty()) {
                                    backStack.removeLastOrNull()
                                }
                                backStack.add(ValoNavKey.Connect)
                            },
                            onOpenCalculator = {
                                backStack.add(ValoNavKey.VpCalculator)
                            }
                        )
                    }
                    
                    entry<ValoNavKey.Bundles>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { StoreDetailPlaceholder("Select an item from the bundle") }
                        )
                    ) {
                        BundlesScreen(
                            viewModel = storeViewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            },
                            onNavigateToConnect = {
                                loginViewModel.resetState()
                                while (backStack.isNotEmpty()) {
                                    backStack.removeLastOrNull()
                                }
                                backStack.add(ValoNavKey.Connect)
                            }
                        )
                    }
                    
                    entry<ValoNavKey.NightMarket>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { StoreDetailPlaceholder("Select a discounted skin") }
                        )
                    ) {
                        NightMarketScreen(
                            viewModel = storeViewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            },
                            onNavigateToConnect = {
                                loginViewModel.resetState()
                                while (backStack.isNotEmpty()) {
                                    backStack.removeLastOrNull()
                                }
                                backStack.add(ValoNavKey.Connect)
                            }
                        )
                    }

                    entry<ValoNavKey.Catalog>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { StoreDetailPlaceholder("Select a skin from the catalog") }
                        )
                    ) {
                        val viewModel: CatalogViewModel = hiltViewModel()
                        CatalogScreen(
                            viewModel = viewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            }
                        )
                    }

                    entry<ValoNavKey.Wishlist>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { StoreDetailPlaceholder("Select a wishlisted skin") }
                        )
                    ) {
                        val viewModel: CatalogViewModel = hiltViewModel()
                        WishlistScreen(
                            viewModel = viewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            },
                            onOpenCalculator = {
                                backStack.add(ValoNavKey.VpCalculator)
                            }
                        )
                    }

                    entry<ValoNavKey.VpCalculator> {
                        VpCalculatorScreen(
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<ValoNavKey.Settings> {
                        val viewModel: SettingsViewModel = hiltViewModel()
                        SettingsScreen(
                            viewModel = viewModel,
                            onLogout = {
                                loginViewModel.resetState()
                                while (backStack.isNotEmpty()) {
                                    backStack.removeLastOrNull()
                                }
                                backStack.add(ValoNavKey.Connect)
                            },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<ValoNavKey.Friends> {
                        FriendsScreen(
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    
                    entry<ValoNavKey.StoreDetail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { key ->
                        StoreDetailScreen(
                            item = key.item,
                            viewModel = storeViewModel,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun WalletBadgeBar(
    wallet: UserWallet,
    onVpClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getIntegerInstance(Locale.US) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Valorant Points (VP) - Clickable to open VP Calculator!
        WalletChip(
            iconUrl = "https://media.valorant-api.com/currencies/85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741/displayicon.png",
            amount = numberFormat.format(wallet.vp),
            contentDescription = "Valorant Points (Tap to open Calculator)",
            onClick = onVpClick
        )

        // Radianite Points (RP)
        WalletChip(
            iconUrl = "https://media.valorant-api.com/currencies/e59aa87c-4cbf-517a-5983-6e81511be9b7/displayicon.png",
            amount = numberFormat.format(wallet.radianite),
            contentDescription = "Radianite Points"
        )

        // Kingdom Credits (KC)
        WalletChip(
            iconUrl = "https://media.valorant-api.com/currencies/85ca954a-41f2-ce94-9b45-8ca3dd39a00d/displayicon.png",
            amount = numberFormat.format(wallet.kingdomCredits),
            contentDescription = "Kingdom Credits"
        )
    }
}

@Composable
fun WalletChip(
    iconUrl: String,
    amount: String,
    contentDescription: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF141C26).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color(0xFF263445).copy(alpha = 0.6f)),
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.size(14.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = amount,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ValoNavigationBar(
    currentKey: NavKey,
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = currentKey is ValoNavKey.StoreRotation || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.StoreRotation }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.StoreRotation }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.StoreRotation)
            },
            icon = { Icon(Icons.Rounded.Storefront, contentDescription = "Store") },
            label = { Text("Store") }
        )
        NavigationBarItem(
            selected = currentKey is ValoNavKey.Bundles || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.Bundles }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.Bundles }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.Bundles)
            },
            icon = { Icon(Icons.Rounded.ViewCarousel, contentDescription = "Bundles") },
            label = { Text("Bundles") }
        )
        NavigationBarItem(
            selected = currentKey is ValoNavKey.NightMarket || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.NightMarket }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.NightMarket }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.NightMarket)
            },
            icon = { Icon(Icons.Rounded.Nightlight, contentDescription = "Market") },
            label = { Text("Market") }
        )
        NavigationBarItem(
            selected = currentKey is ValoNavKey.Catalog || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.Catalog }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.Catalog }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.Catalog)
            },
            icon = { Icon(Icons.AutoMirrored.Rounded.List, contentDescription = "Catalog") },
            label = { Text("Catalog") }
        )
        NavigationBarItem(
            selected = currentKey is ValoNavKey.Wishlist || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.Wishlist }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.Wishlist }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.Wishlist)
            },
            icon = { Icon(Icons.Rounded.Favorite, contentDescription = "Wishlist") },
            label = { Text("Wishlist") }
        )
    }
}

@Composable
fun ValoNavigationRail(
    currentKey: NavKey,
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier
) {
    NavigationRail(modifier = modifier) {
        NavigationRailItem(
            selected = currentKey is ValoNavKey.StoreRotation || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.StoreRotation }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.StoreRotation }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.StoreRotation)
            },
            icon = { Icon(Icons.Rounded.Storefront, contentDescription = "Store") },
            label = { Text("Store") }
        )
        NavigationRailItem(
            selected = currentKey is ValoNavKey.Bundles || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.Bundles }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.Bundles }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.Bundles)
            },
            icon = { Icon(Icons.Rounded.ViewCarousel, contentDescription = "Bundles") },
            label = { Text("Bundles") }
        )
        NavigationRailItem(
            selected = currentKey is ValoNavKey.NightMarket || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.NightMarket }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.NightMarket }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.NightMarket)
            },
            icon = { Icon(Icons.Rounded.Nightlight, contentDescription = "Market") },
            label = { Text("Market") }
        )
        NavigationRailItem(
            selected = currentKey is ValoNavKey.Catalog || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.Catalog }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.Catalog }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.Catalog)
            },
            icon = { Icon(Icons.AutoMirrored.Rounded.List, contentDescription = "Catalog") },
            label = { Text("Catalog") }
        )
        NavigationRailItem(
            selected = currentKey is ValoNavKey.Wishlist || (currentKey is ValoNavKey.StoreDetail && backStack.any { it is ValoNavKey.Wishlist }),
            onClick = { 
                backStack.removeIf { it !is ValoNavKey.Wishlist }
                if (backStack.isEmpty()) backStack.add(ValoNavKey.Wishlist)
            },
            icon = { Icon(Icons.Rounded.Favorite, contentDescription = "Wishlist") },
            label = { Text("Wishlist") }
        )
    }
}

@Composable
fun StoreDetailPlaceholder(message: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun ValoAppTabletPreview() {
    MaterialTheme {
        ValoApp()
    }
}
