package com.example.valomobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.R
import com.example.valomobile.domain.model.SkinItem

@Composable
fun SkinItemCard(
    item: SkinItem,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic tier colors based on Valorant rarity
    val tierColor = when {
        item.tier.contains("Ultra", ignoreCase = true) || item.tier.contains("Exclusive", ignoreCase = true) -> Color(0xFFFFB800) // Gold / Amber
        item.tier.contains("Premium", ignoreCase = true) -> Color(0xFFFF4655) // Riot Crimson
        item.tier.contains("Deluxe", ignoreCase = true) -> Color(0xFF00E5FF) // Cyber Cyan
        else -> Color(0xFF818CF8) // Electric Indigo / Select
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = tierColor.copy(alpha = 0.35f),
                ambientColor = tierColor.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tierColor.copy(alpha = 0.7f),
                        Color(0xFF1E2836).copy(alpha = 0.4f),
                        tierColor.copy(alpha = 0.25f)
                    )
                )
            )
            .padding(1.5.dp) // Subtle Glowing Tier Border
            .clip(RoundedCornerShape(14.5.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161F2A),
                        Color(0xFF0E141C),
                        Color(0xFF080C10)
                    )
                )
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Weapon Preview Box with Radial Ambient Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B1017)),
                contentAlignment = Alignment.Center
            ) {
                // Radial Glow behind weapon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    tierColor.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Weapon Render
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
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    contentScale = ContentScale.Fit
                )

                // Top Left: Rarity Tier Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF000000).copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(tierColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.tier.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Top Right: Wishlist Heart
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF000000).copy(alpha = 0.45f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = onWishlistToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) Color(0xFFFF4655) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom Left: Discount Badge (if any)
                if (item.discount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF4655),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "-${item.discount}%",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skin Name
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Footer: Weapon Type & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.weaponType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(horizontalAlignment = Alignment.End) {
                    if (item.discount > 0) {
                        Text(
                            text = "${item.price} VP",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (item.finalPrice > 0) "${item.finalPrice}" else "Varies",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = if (item.discount > 0) Color(0xFF00FF88) else Color(0xFFFF4655)
                        )
                        if (item.finalPrice > 0) {
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
            }
        }
    }
}
