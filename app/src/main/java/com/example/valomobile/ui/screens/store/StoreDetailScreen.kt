package com.example.valomobile.ui.screens.store

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.R
import com.example.valomobile.data.remote.model.ValorantChroma
import com.example.valomobile.data.remote.model.ValorantLevel
import com.example.valomobile.domain.model.SkinItem

@Composable
fun StoreDetailScreen(
    item: SkinItem,
    viewModel: StoreViewModel,
    detailViewModel: SkinDetailViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val wishlist by viewModel.wishlist.collectAsState()
    val isWishlisted = wishlist.contains(item.skinUuid)
    val uiState by detailViewModel.uiState.collectAsState()

    LaunchedEffect(item) {
        detailViewModel.loadSkinDetails(item)
    }

    val tierColor = when {
        item.tier.contains("Ultra", ignoreCase = true) || item.tier.contains("Exclusive", ignoreCase = true) -> Color(0xFFFFB800)
        item.tier.contains("Premium", ignoreCase = true) -> Color(0xFFFF4655)
        item.tier.contains("Deluxe", ignoreCase = true) -> Color(0xFF00E5FF)
        else -> Color(0xFF818CF8)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                IconButton(
                    onClick = { viewModel.toggleWishlist(item) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) Color(0xFFFF4655) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is SkinDetailState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = tierColor)
                    }
                }
                is SkinDetailState.Success -> {
                    // MAIN MEDIA PREVIEW (Video or Static Render)
                    if (!state.activeVideoUrl.isNullOrBlank()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Videocam, 
                                    contentDescription = null, 
                                    tint = tierColor, 
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IN-GAME SHOWCASE & VFX",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = tierColor,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            SkinVideoPlayer(
                                videoUrl = state.activeVideoUrl,
                                tierColor = tierColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                            )
                        }
                    } else {
                        // Static High-Res Render Box
                        val renderIcon = state.selectedChroma?.fullRender 
                            ?: state.selectedChroma?.displayIcon 
                            ?: item.displayIcon

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    spotColor = tierColor.copy(alpha = 0.35f),
                                    ambientColor = tierColor.copy(alpha = 0.2f)
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF161F2A),
                                            Color(0xFF0E141C)
                                        )
                                    )
                                )
                                .border(1.5.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                tierColor.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(renderIcon)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.displayName,
                                placeholder = painterResource(R.drawable.placeholder),
                                error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // CHROMAS / COLOR VARIANTS SECTION
                    if (state.skin.chromas.size > 1) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "COLOR VARIANTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(state.skin.chromas) { chroma ->
                                    val isSelected = chroma.uuid == state.selectedChroma?.uuid
                                    ChromaChip(
                                        chroma = chroma,
                                        isSelected = isSelected,
                                        tierColor = tierColor,
                                        onClick = { detailViewModel.selectChroma(chroma) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // LEVELS & ANIMATION UPGRADES SECTION
                    if (state.skin.levels.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "UPGRADE LEVELS & EFFECTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(state.skin.levels) { level ->
                                    val isSelected = level.uuid == state.selectedLevel?.uuid
                                    val hasVideo = !level.streamedVideo.isNullOrBlank()
                                    LevelChip(
                                        level = level,
                                        isSelected = isSelected,
                                        hasVideo = hasVideo,
                                        tierColor = tierColor,
                                        onClick = { detailViewModel.selectLevel(level) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                is SkinDetailState.Error -> {
                    // Fallback to static preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141A22)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.displayIcon)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.displayName,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // PRICE & RARITY INFO CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = tierColor.copy(alpha = 0.25f),
                        ambientColor = tierColor.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1A232E),
                                Color(0xFF0F1620)
                            )
                        )
                    )
                    .border(1.dp, tierColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(tierColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${item.tier.uppercase()} EDITION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierColor,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.weaponType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val basePrice = when {
                        item.price > 0 -> item.price
                        item.finalPrice > 0 -> item.finalPrice
                        item.tier.contains("Ultra", ignoreCase = true) -> 2475
                        item.tier.contains("Exclusive", ignoreCase = true) -> 2175
                        item.tier.contains("Premium", ignoreCase = true) -> 1775
                        item.tier.contains("Deluxe", ignoreCase = true) -> 1275
                        item.tier.contains("Select", ignoreCase = true) -> 875
                        item.weaponType.contains("Buddy", ignoreCase = true) -> 475
                        item.weaponType.contains("Card", ignoreCase = true) -> 375
                        item.weaponType.contains("Spray", ignoreCase = true) -> 325
                        else -> 1775
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (item.discount > 0 && item.price > 0 && item.finalPrice > 0 && item.price != item.finalPrice) {
                            Text(
                                text = "${item.price} VP",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val priceToShow = if (item.discount > 0 && item.finalPrice > 0 && item.finalPrice != basePrice) item.finalPrice else basePrice
                            Text(
                                text = "$priceToShow",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = if (item.discount > 0 && item.finalPrice < basePrice) Color(0xFF00FF88) else Color(0xFFFF4655)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SkinVideoPlayer(
    videoUrl: String,
    tierColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }

    // Single dedicated ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }

    // Attach player listener & clean up on dispose
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Gracefully handle switching video sources without creating duplicate players
    LaunchedEffect(videoUrl) {
        isBuffering = true
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // App Lifecycle observer (pause on background, resume on foreground)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.stop()
                    exoPlayer.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = tierColor.copy(alpha = 0.4f),
                ambientColor = tierColor.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black)
            .border(1.5.dp, tierColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering / Loading Indicator
        if (isBuffering) {
            CircularProgressIndicator(
                color = tierColor,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }

        // Overlay Controls (Mute & Play/Pause)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    exoPlayer.volume = if (isMuted) 0f else 1f
                },
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
            ) {
                Icon(
                    if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                    contentDescription = "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }

            IconButton(
                onClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
            ) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
fun ChromaChip(
    chroma: ValorantChroma,
    isSelected: Boolean,
    tierColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .height(74.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF1E2838) else Color(0xFF101720))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) tierColor else Color(0xFF2B3848),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(chroma.swatches ?: chroma.displayIcon)
                    .crossfade(true)
                    .build(),
                contentDescription = chroma.displayName,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chroma.displayName.substringAfterLast(" (").replace(")", ""),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.Gray,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LevelChip(
    level: ValorantLevel,
    isSelected: Boolean,
    hasVideo: Boolean,
    tierColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF1E2838) else Color(0xFF101720))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) tierColor else Color(0xFF2B3848),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasVideo) {
                Icon(
                    Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    tint = if (isSelected) tierColor else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = level.displayName ?: "Level",
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}
