package com.example.valomobile.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.R
import com.example.valomobile.domain.model.Bundle
import com.example.valomobile.domain.model.SkinItem

@Composable
fun BundlesScreen(
    viewModel: StoreViewModel,
    onItemClick: (SkinItem) -> Unit,
    onNavigateToConnect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bundles by viewModel.featuredBundles.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFF4655))
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Loading bundles...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else if (error != null) {
        val isSessionExpired = error?.contains("session", ignoreCase = true) == true
            || error?.contains("expired", ignoreCase = true) == true
            || error?.contains("log in", ignoreCase = true) == true
            || error?.contains("token", ignoreCase = true) == true
            || !viewModel.isLoggedIn

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (isSessionExpired && onNavigateToConnect != null) {
                    Button(
                        onClick = onNavigateToConnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4655),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Your Shop (Reconnect)", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { viewModel.loadData() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Refresh", fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4655),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh Shop", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else if (bundles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No bundles available at the moment.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Featured Bundles",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(bundles) { bundle ->
                BundleCard(
                    bundle = bundle,
                    wishlist = wishlist,
                    onWishlistToggle = { viewModel.toggleWishlist(it) },
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun BundleCard(
    bundle: Bundle,
    wishlist: Set<String>,
    onWishlistToggle: (SkinItem) -> Unit,
    onItemClick: (SkinItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFFF4655).copy(alpha = 0.3f),
                ambientColor = Color(0xFFFF4655).copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF4655).copy(alpha = 0.4f),
                        Color(0xFF1E2836).copy(alpha = 0.3f),
                        Color(0xFFFF4655).copy(alpha = 0.2f)
                    )
                )
            )
            .padding(1.5.dp)
            .clip(RoundedCornerShape(18.5.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF161F2A),
                        Color(0xFF0F1620),
                        Color(0xFF0A0F14)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF0B1017)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(bundle.displayIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = bundle.displayName,
                    placeholder = painterResource(R.drawable.placeholder),
                    error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF4655),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "${bundle.price} VP",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = bundle.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                if (bundle.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bundle.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "BUNDLE CONTENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF4655),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${bundle.items.size} ITEMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    bundle.items.forEach { item ->
                        val isWishlisted = wishlist.contains(item.skinUuid)
                        BundleItemCard(
                            item = item,
                            isWishlisted = isWishlisted,
                            onWishlistToggle = { onWishlistToggle(item) },
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BundleItemCard(
    item: SkinItem,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111722))
            .border(1.dp, Color(0xFF222F40), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 86.dp, height = 54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF080D14)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.displayIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.displayName,
                    placeholder = painterResource(R.drawable.placeholder),
                    error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1
                )
                val basePrice = when {
                    item.price > 0 -> item.price
                    item.finalPrice > 0 -> item.finalPrice
                    item.weaponType.contains("Buddy", ignoreCase = true) -> 475
                    item.weaponType.contains("Card", ignoreCase = true) -> 375
                    item.weaponType.contains("Spray", ignoreCase = true) -> 325
                    item.weaponType.contains("Flex", ignoreCase = true) -> 1350
                    else -> 1775
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$basePrice VP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4655)
                    )
                }
            }

            IconButton(
                onClick = onWishlistToggle,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF080D14))
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) Color(0xFFFF4655) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
