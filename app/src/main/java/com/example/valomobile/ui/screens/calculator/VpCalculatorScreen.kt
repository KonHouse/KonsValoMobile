package com.example.valomobile.ui.screens.calculator

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.R
import com.example.valomobile.domain.model.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpCalculatorScreen(
    onBack: () -> Unit,
    viewModel: VpCalculatorViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val selectedSkins by viewModel.selectedSkins.collectAsState()
    val currentVp by viewModel.currentVp.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isNightMarketAvailable by viewModel.isNightMarketAvailable.collectAsState()
    val result by viewModel.calculationResult.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var showEditVpDialog by remember { mutableStateOf(false) }
    var showAddSkinSheet by remember { mutableStateOf(false) }

    val numberFormat = remember { NumberFormat.getIntegerInstance(Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VP Calculator", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Currency Selector (PLN / EUR)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF141C26),
                        border = BorderStroke(1.dp, Color(0xFF253346)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            CurrencyType.entries.forEach { curr ->
                                val isSelected = curr == currency
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0xFFFF4655) else Color.Transparent)
                                        .clickable { viewModel.setCurrency(curr) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = curr.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    if (selectedSkins.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // 1. HERO CALCULATION RESULT CARD
            item {
                HeroResultCard(
                    selectedSkinsCount = selectedSkins.size,
                    result = result,
                    currentVp = currentVp,
                    currency = currency,
                    onEditCurrentVp = { showEditVpDialog = true },
                    numberFormat = numberFormat
                )
            }

            // 2. QUICK ACTIONS & IMPORT
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Row 1: Add Skin + Add Current Bundle + Night Market
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showAddSkinSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF4655),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Skin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.importFeaturedBundles() },
                            modifier = Modifier.weight(1.1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.ViewCarousel, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB800))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Bundle", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (isNightMarketAvailable) {
                            OutlinedButton(
                                onClick = { viewModel.importNightMarket() },
                                modifier = Modifier.weight(1.2f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Rounded.Nightlight, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Market", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Row 2: Wishlist + Daily Shop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.importWishlist() },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Favorite, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFFF4655))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Wishlist", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.importStoreRotation() },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Storefront, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Daily Shop", fontSize = 11.sp)
                        }
                    }
                }
            }

            // 3. SELECTED SKINS HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECTED ITEMS (${selectedSkins.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    if (selectedSkins.isNotEmpty()) {
                        Text(
                            text = "${numberFormat.format(result.totalCostVp)} VP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4655)
                        )
                    }
                }
            }

            // 4. SELECTED SKINS LIST OR EMPTY STATE
            if (selectedSkins.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF10151E))
                            .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Cart is empty",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Add skins, bundle, or night market items to calculate",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(selectedSkins, key = { it.uuid }) { skin ->
                    CompactSkinRow(
                        skin = skin,
                        onRemove = { viewModel.removeSkin(skin) }
                    )
                }
            }
        }
    }

    // DIALOG: EDIT CURRENT VP
    if (showEditVpDialog) {
        var tempVpText by remember { mutableStateOf(currentVp.toString()) }
        AlertDialog(
            onDismissRequest = { showEditVpDialog = false },
            title = { Text("Your Current VP Balance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your in-game VP amount to calculate missing points:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempVpText,
                        onValueChange = { tempVpText = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = { Text("VP", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempVpText.toIntOrNull() ?: 0
                        viewModel.setCurrentVp(parsed)
                        showEditVpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditVpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // BOTTOM SHEET: ADD SKIN SEARCH
    if (showAddSkinSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSkinSheet = false
                viewModel.setSearchQuery("")
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add Skin to Calculator",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search skin (e.g. Vandal, Phantom, Kuronami)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(searchResults, key = { it.uuid }) { skin ->
                            val isSelected = selectedSkins.any { it.uuid == skin.uuid }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF131A24),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isSelected) {
                                            viewModel.addSkin(skin)
                                            showAddSkinSheet = false
                                            viewModel.setSearchQuery("")
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(skin.displayIcon)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = skin.displayName,
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = skin.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${skin.price} VP",
                                            color = Color(0xFFFF4655),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Text("Added", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Rounded.AddCircle, contentDescription = "Add", tint = Color(0xFFFF4655), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                } else if (searchQuery.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No skins found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun HeroResultCard(
    selectedSkinsCount: Int,
    result: VpCalculationResult,
    currentVp: Int,
    currency: CurrencyType,
    onEditCurrentVp: () -> Unit,
    numberFormat: NumberFormat
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF121822),
        border = BorderStroke(1.dp, Color(0xFF202C3D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedSkinsCount == 0) {
                // Empty state prompt
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Calculate,
                        contentDescription = null,
                        tint = Color(0xFFFF4655),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VP Purchase Optimizer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Add skins, bundle, or night market items below.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (result.isAlreadyAffordable) {
                // Sufficient VP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00FF88),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "You have enough VP!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Remaining after purchase: +${numberFormat.format(result.leftoverVp)} VP",
                            fontSize = 12.sp,
                            color = Color(0xFF00FF88)
                        )
                    }
                }
            } else {
                // Missing VP -> Show Clean Recommendation
                val priceStr = if (currency == CurrencyType.PLN) {
                    "${String.format(Locale.US, "%.2f", result.totalCostPln)} zł"
                } else {
                    "€${String.format(Locale.US, "%.2f", result.totalCostEur)}"
                }

                // 1. Total to Pay + Leftover
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "TOTAL TO PAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = priceStr,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF00FF88)
                        )
                    }

                    if (result.leftoverVp > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF17281F),
                            border = BorderStroke(1.dp, Color(0xFF204832))
                        ) {
                            Text(
                                text = "+${numberFormat.format(result.leftoverVp)} VP leftover",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF88),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 2. Recommended Packs to Buy
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BUY THESE PACKS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFB800),
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.recommendedPacks.forEach { item ->
                            val itemPriceStr = if (currency == CurrencyType.PLN) {
                                "${String.format(Locale.US, "%.2f", item.pack.pricePln * item.quantity)} zł"
                            } else {
                                "€${String.format(Locale.US, "%.2f", item.pack.priceEur * item.quantity)}"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0B1017),
                                border = BorderStroke(1.dp, Color(0xFF1E2838)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${item.quantity}x ${numberFormat.format(item.pack.totalVp)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = itemPriceStr,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E2838))

            // 3. Compact Status Bar (Cart Total • Balance • Missing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cart Total
                Column {
                    Text(
                        text = "CART TOTAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${numberFormat.format(result.totalCostVp)} VP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Current VP (Clickable to edit)
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onEditCurrentVp)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "YOUR VP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    Text(
                        text = "${numberFormat.format(currentVp)} VP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF88)
                    )
                }

                // Missing VP
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MISSING",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (result.missingVp > 0) "${numberFormat.format(result.missingVp)} VP" else "0 VP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.missingVp > 0) Color(0xFFFF4655) else Color(0xFF00FF88)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactSkinRow(
    skin: SkinItem,
    onRemove: () -> Unit
) {
    val isBundle = skin.weaponType.equals("Bundle", ignoreCase = true)
    val hasDiscount = skin.discount > 0

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF10151E),
        border = BorderStroke(1.dp, if (isBundle) Color(0xFFFFB800).copy(alpha = 0.35f) else Color(0xFF1C2634)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF080C12)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(skin.displayIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = skin.displayName,
                    placeholder = painterResource(R.drawable.placeholder),
                    error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                    modifier = Modifier.fillMaxSize().padding(2.dp),
                    contentScale = if (isBundle) ContentScale.Crop else ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = skin.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isBundle) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFB800).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "BUNDLE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFB800),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val priceToShow = if (skin.finalPrice > 0) skin.finalPrice else skin.price
                    Text(
                        text = "$priceToShow VP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4655)
                    )
                    if (hasDiscount) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00FF88).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "-${skin.discount}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF88),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Remove",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
