package com.example.valomobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.valomobile.ui.navigation.ValoNavKey
import com.example.valomobile.ui.screens.catalog.CatalogScreen
import com.example.valomobile.ui.screens.catalog.CatalogViewModel
import com.example.valomobile.ui.screens.catalog.WishlistScreen
import com.example.valomobile.ui.screens.login.RiotLoginScreen
import com.example.valomobile.ui.screens.login.RiotLoginViewModel
import com.example.valomobile.ui.screens.settings.SettingsScreen
import com.example.valomobile.ui.screens.settings.SettingsViewModel
import com.example.valomobile.ui.screens.store.*

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ValoApp() {
    val loginViewModel: RiotLoginViewModel = hiltViewModel()
    val isLoggedIn = loginViewModel.isLoggedIn()
    
    val backStack = rememberNavBackStack(
        if (isLoggedIn) ValoNavKey.StoreRotation as NavKey else ValoNavKey.Connect as NavKey
    )
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    
    val currentKey = backStack.last()
    val showNavigation = currentKey !is ValoNavKey.Connect && currentKey !is ValoNavKey.Settings
    val showTopBar = currentKey !is ValoNavKey.Connect && currentKey !is ValoNavKey.Settings
    
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val useRail = showNavigation && adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

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
                        title = { Text("ValoMobile") },
                        actions = {
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
                        val viewModel: StoreViewModel = hiltViewModel()
                        StoreRotationScreen(
                            viewModel = viewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            }
                        )
                    }
                    
                    entry<ValoNavKey.Bundles>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { StoreDetailPlaceholder("Select an item from the bundle") }
                        )
                    ) {
                        val viewModel: StoreViewModel = hiltViewModel()
                        BundlesScreen(
                            viewModel = viewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            }
                        )
                    }
                    
                    entry<ValoNavKey.NightMarket>(
                        metadata = ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { StoreDetailPlaceholder("Select a discounted skin") }
                        )
                    ) {
                        val viewModel: StoreViewModel = hiltViewModel()
                        NightMarketScreen(
                            viewModel = viewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
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
                            detailPlaceholder = { StoreDetailPlaceholder("Select a skin from your wishlist") }
                        )
                    ) {
                        val viewModel: CatalogViewModel = hiltViewModel()
                        WishlistScreen(
                            viewModel = viewModel,
                            onItemClick = { item ->
                                backStack.add(ValoNavKey.StoreDetail(item))
                            }
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
                    
                    entry<ValoNavKey.StoreDetail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { key ->
                        val viewModel: StoreViewModel = hiltViewModel()
                        StoreDetailScreen(
                            item = key.item,
                            viewModel = viewModel,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ValoNavigationBar(
    currentKey: NavKey,
    backStack: NavBackStack<NavKey>
) {
    NavigationBar {
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
    backStack: NavBackStack<NavKey>
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        header = {
            Icon(
                Icons.Rounded.Shield,
                contentDescription = null,
                modifier = Modifier.padding(vertical = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    ) {
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
