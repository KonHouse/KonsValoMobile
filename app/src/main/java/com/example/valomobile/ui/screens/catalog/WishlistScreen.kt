package com.example.valomobile.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.domain.model.SkinItem

@Composable
fun WishlistScreen(
    viewModel: CatalogViewModel,
    onItemClick: (SkinItem) -> Unit
) {
    val items by viewModel.wishlistItems.collectAsState()

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Your wishlist is empty", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.uuid }) { skin ->
                WishlistItem(
                    skin = skin,
                    onRemove = { viewModel.removeFromWishlist(skin.uuid) },
                    onClick = { onItemClick(skin) }
                )
            }
        }
    }
}

@Composable
fun WishlistItem(
    skin: SkinItem,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val tierColor = when {
        skin.tier.contains("Ultra", ignoreCase = true) || skin.tier.contains("Exclusive", ignoreCase = true) -> Color(0xFFFFB800)
        skin.tier.contains("Premium", ignoreCase = true) -> Color(0xFFFF4655)
        skin.tier.contains("Deluxe", ignoreCase = true) -> Color(0xFF00E5FF)
        else -> Color(0xFF818CF8)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = tierColor.copy(alpha = 0.25f),
                ambientColor = tierColor.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tierColor.copy(alpha = 0.5f),
                        Color(0xFF1E2836).copy(alpha = 0.3f),
                        tierColor.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(1.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161F2A),
                        Color(0xFF0E141C)
                    )
                )
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weapon Icon Box
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B1017)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(skin.displayIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = skin.displayName,
                    modifier = Modifier
                        .size(70.dp)
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = skin.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (skin.finalPrice > 0) "${skin.finalPrice}" else "Varies",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFFFF4655)
                    )
                    if (skin.finalPrice > 0) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "VP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Remove from wishlist",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
