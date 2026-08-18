package com.example.valomobile.ui.screens.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.R
import com.example.valomobile.domain.model.Bundle
import com.example.valomobile.domain.model.SkinItem

@Composable
fun BundlesScreen(
    viewModel: StoreViewModel,
    onItemClick: (SkinItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val bundles by viewModel.featuredBundles.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error!!, color = Color.Red)
        }
    } else if (bundles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No bundles available at the moment.")
        }
    } else {
        // Use a grid for bundles on wider screens, otherwise a list
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 350.dp),
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Featured Bundles",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(bundle.displayIcon)
                    .crossfade(true)
                    .build(),
                contentDescription = bundle.displayName,
                placeholder = painterResource(R.drawable.placeholder),
                error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = bundle.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = bundle.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${bundle.price} VP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Items in Bundle",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                bundle.items.forEach { item ->
                    val isWishlisted = wishlist.contains(item.skinUuid)
                    ListItem(
                        headlineContent = { Text(item.displayName) },
                        supportingContent = { Text("${item.price} VP") },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onWishlistToggle(item) }) {
                                    Icon(
                                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Wishlist",
                                        tint = if (isWishlisted) Color.Red else LocalContentColor.current
                                    )
                                }
                                Button(onClick = { onItemClick(item) }) {
                                    Text("View")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
