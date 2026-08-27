package com.example.valomobile.ui.screens.friends

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.domain.model.CloudStoreSkinOffer
import com.example.valomobile.domain.model.FriendInvite
import com.example.valomobile.domain.model.InAppFriendItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val myFriendCode by viewModel.myFriendCode.collectAsState()
    val friendsList by viewModel.filteredFriends.collectAsState()
    val rawFriendsList by viewModel.friendsList.collectAsState()
    val incomingInvites by viewModel.incomingInvites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val inviteInput by viewModel.inviteInput.collectAsState()
    val isSendingInvite by viewModel.isSendingInvite.collectAsState()
    val selectedFriendForStore by viewModel.selectedFriendForStoreModal.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Friends (${rawFriendsList.size})", "Invites (${incomingInvites.size})", "Add Friend")

    // Show Toast for action / error feedback
    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "VALO FRIENDS",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            fontSize = 18.sp
                        )

                        // Friend Code Chip with 1-click copy
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E2836),
                            border = BorderStroke(1.dp, Color(0xFFFF4655).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    copyToClipboard(context, myFriendCode)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = Color(0xFFFF4655),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = myFriendCode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color(0xFFFF4655),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFFF4655)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                // Red pulsing dot if invites tab has pending items
                                if (index == 1 && incomingInvites.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF4655))
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> FriendsListTab(
                    friends = friendsList,
                    searchQuery = searchQuery,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onInspectStore = viewModel::openFriendStoreModal,
                    onAddFriendClick = { selectedTabIndex = 2 },
                    myFriendCode = myFriendCode
                )
                1 -> InvitesTab(
                    invites = incomingInvites,
                    onAccept = viewModel::acceptInvite,
                    onDecline = viewModel::declineInvite
                )
                2 -> AddFriendTab(
                    myFriendCode = myFriendCode,
                    inviteInput = inviteInput,
                    isSending = isSendingInvite,
                    onInputChange = viewModel::onInviteInputChange,
                    onSendInvite = viewModel::sendInvite
                )
            }
        }
    }

    // Friend Store Modal
    selectedFriendForStore?.let { friend ->
        FriendStoreDetailDialog(
            friend = friend,
            onDismiss = viewModel::dismissFriendStoreModal,
            onRemoveFriend = {
                viewModel.removeFriend(friend.puuid)
            }
        )
    }
}

@Composable
private fun FriendsListTab(
    friends: List<InAppFriendItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onInspectStore: (InAppFriendItem) -> Unit,
    onAddFriendClick: () -> Unit,
    myFriendCode: String
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by Riot ID, code or skin...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        if (friends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PeopleOutline,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "No friends added yet" else "No matching friends found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isEmpty())
                            "Share your Friend Code ($myFriendCode) with friends to see their daily Valorant stores!"
                        else
                            "Try searching with a different name or skin.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onAddFriendClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add Friend", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { shareFriendCode(context, myFriendCode) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Share My Code")
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(friends, key = { it.puuid }) { friend ->
                    FriendCard(friend = friend, onInspectStore = { onInspectStore(friend) })
                }
            }
        }
    }
}

@Composable
private fun FriendCard(
    friend: InAppFriendItem,
    onInspectStore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141E28)),
        border = BorderStroke(1.dp, Color(0xFF263342)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectStore() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF4655), Color(0xFF8B1221))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = friend.riotId.take(1).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = friend.riotId,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = friend.friendCode,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            if (friend.currentStreak > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🔥 ${friend.currentStreak}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }

                // Sync time badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (friend.storeOffers.isNotEmpty()) Color(0xFF162E20) else Color(0xFF2A2A2A)
                ) {
                    Text(
                        text = if (friend.storeOffers.isNotEmpty()) "Store Synced" else "No Offers Yet",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (friend.storeOffers.isNotEmpty()) Color(0xFF81C784) else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Daily Skin Offers Row
            if (friend.storeOffers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    friend.storeOffers.take(4).forEach { skin ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(66.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F151C))
                                .border(
                                    width = 1.dp,
                                    color = parseHexColor(skin.tierColor).copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(skin.displayIcon)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = skin.displayName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = skin.displayName,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitesTab(
    invites: List<FriendInvite>,
    onAccept: (FriendInvite) -> Unit,
    onDecline: (FriendInvite) -> Unit
) {
    if (invites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.MailOutline,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No pending friend invites",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "When someone sends you a friend invite using your code, it will appear here.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(invites, key = { it.id }) { invite ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141E28)),
                    border = BorderStroke(1.dp, Color(0xFF263342))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = invite.fromRiotId,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Code: ${invite.fromFriendCode}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { onDecline(invite) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2B1A1E))
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Decline", tint = Color(0xFFFF5252))
                            }
                            IconButton(
                                onClick = { onAccept(invite) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1B2F20))
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFriendTab(
    myFriendCode: String,
    inviteInput: String,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSendInvite: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // My Code Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF121B24),
            border = BorderStroke(1.dp, Color(0xFF243447)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR FRIEND CODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = myFriendCode,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = Color(0xFFFF4655)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { copyToClipboard(context, myFriendCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2836)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Code")
                    }
                    Button(
                        onClick = { shareFriendCode(context, myFriendCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        }

        // Add Friend Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF121B24),
            border = BorderStroke(1.dp, Color(0xFF243447)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ENTER FRIEND'S CODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inviteInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("e.g. VALO-7X9K or Player#EUW") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onSendInvite,
                    enabled = !isSending && inviteInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Friend Invite", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendStoreDetailDialog(
    friend: InAppFriendItem,
    onDismiss: () -> Unit,
    onRemoveFriend: () -> Unit
) {
    var showConfirmRemove by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${friend.riotId}'s Store",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Friend Code: ${friend.friendCode}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = { showConfirmRemove = true }) {
                    Icon(Icons.Rounded.PersonRemove, contentDescription = "Remove Friend", tint = Color(0xFFFF5252))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (friend.storeOffers.isEmpty()) {
                    Text(
                        text = "This friend hasn't opened their ValoMobile store yet today.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                } else {
                    friend.storeOffers.forEach { skin ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F151C),
                            border = BorderStroke(1.dp, parseHexColor(skin.tierColor).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(skin.displayIcon)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = skin.displayName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(width = 70.dp, height = 40.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = skin.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${skin.price} VP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF4655)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showConfirmRemove) {
        AlertDialog(
            onDismissRequest = { showConfirmRemove = false },
            title = { Text("Remove Friend?") },
            text = { Text("Are you sure you want to remove ${friend.riotId} from your friends list?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRemove = false
                        onRemoveFriend()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRemove = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun copyToClipboard(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Friend Code", code)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied $code to clipboard!", Toast.LENGTH_SHORT).show()
}

private fun shareFriendCode(context: Context, code: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Add me on ValoMobile to check my daily Valorant store! My Friend Code is: $code")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Friend Code")
    context.startActivity(shareIntent)
}

private fun parseHexColor(colorStr: String?): Color {
    if (colorStr.isNullOrBlank()) return Color(0xFFFF4655)
    return try {
        val clean = colorStr.trim().removePrefix("#")
        if (clean.length == 6) {
            Color(android.graphics.Color.parseColor("#$clean"))
        } else if (clean.length == 8) {
            Color(android.graphics.Color.parseColor("#$clean"))
        } else {
            Color(0xFFFF4655)
        }
    } catch (e: Exception) {
        Color(0xFFFF4655)
    }
}
