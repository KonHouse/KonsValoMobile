package com.example.valomobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.valomobile.domain.model.DailyStreakInfo
import com.example.valomobile.domain.model.SkinItem
import com.example.valomobile.domain.model.WeekDayStatus
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StreakChip(
    streakCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (streakCount > 0) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePulse"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF22160C).copy(alpha = 0.9f),
        border = BorderStroke(
            1.dp,
            if (streakCount > 0) Color(0xFFFF9800).copy(alpha = 0.7f) else Color(0xFF3E2D1F)
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
        ) {
            Icon(
                Icons.Rounded.LocalFireDepartment,
                contentDescription = "Daily Streak",
                tint = if (streakCount > 0) Color(0xFFFF7A00) else Color(0xFF888888),
                modifier = Modifier
                    .size(15.dp)
                    .scale(pulseScale)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "$streakCount",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (streakCount > 0) Color(0xFFFFD54F) else Color.White
            )
        }
    }
}

@Composable
fun DailyStreakCelebrationDialog(
    streak: Int,
    wasBroken: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1923)),
            border = BorderStroke(1.5.dp, Color(0xFFFF7A00).copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0xFFFF7A00),
                    ambientColor = Color(0xFFFF5722)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fiery Ambient Glow behind Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFF7A00).copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF261608),
                        border = BorderStroke(2.dp, Color(0xFFFF7A00)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (wasBroken) "STREAK RESTARTED!" else "DAILY STREAK ACTIVE!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFF9800)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$streak ${if (streak == 1) "Day" else "Days"} Streak! 🔥",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (wasBroken) {
                        "You missed yesterday, but you're back! Check in tomorrow to keep the flame burning."
                    } else {
                        "Awesome job! You've checked your Valorant store today. Come back tomorrow for Day ${streak + 1}!"
                    },
                    fontSize = 13.sp,
                    color = Color(0xFFB0B9C2),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "LET'S GO",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DailyStreakDetailDialog(
    streakInfo: DailyStreakInfo,
    onViewFullHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1923)),
            border = BorderStroke(1.2.dp, Color(0xFF2E3E52).copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF7A00),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAILY STREAK",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Counter Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF16212E),
                    border = BorderStroke(1.dp, Color(0xFFFF7A00).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${streakInfo.currentStreak}",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔥",
                                fontSize = 34.sp
                            )
                        }

                        Text(
                            text = if (streakInfo.currentStreak == 1) "Day Streak Active" else "Days Streak Active",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Best Record Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF222F3E),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Best Record: ${streakInfo.maxStreak} ${if (streakInfo.maxStreak == 1) "day" else "days"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Weekly Calendar Tracker Header
                Text(
                    text = "THIS WEEK'S ACTIVITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF7A8B9E),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Weekly Days Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    streakInfo.weekDays.forEach { day ->
                        WeekDayItem(day = day)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // VIEW FULL HISTORY BUTTON
                OutlinedButton(
                    onClick = onViewFullHistory,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, Color(0xFFFF7A00).copy(alpha = 0.85f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF261908).copy(alpha = 0.6f),
                        contentColor = Color(0xFFFF9800)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIEW OFFERS HISTORY",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 12.sp,
                        color = Color(0xFFFFD54F)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Informational Tip Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131B24),
                    border = BorderStroke(1.dp, Color(0xFF263545)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "The Valorant daily shop refreshes every day. Launch ValoMobile daily to keep your streak glowing and archive your shop!",
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF9AA7B5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222F3E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text(
                        text = "CLOSE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StoreHistoryCalendarDialog(
    recordedDates: Set<String>,
    onLoadHistoryForDate: suspend (LocalDate) -> List<SkinItem>?,
    onDismiss: () -> Unit
) {
    val today = remember { LocalDate.now(ZoneOffset.UTC) }
    var currentMonth by remember { mutableStateOf(YearMonth.now(ZoneOffset.UTC)) }
    var selectedDate by remember { mutableStateOf(LocalDate.now(ZoneOffset.UTC)) }
    var selectedDateSkins by remember { mutableStateOf<List<SkinItem>?>(null) }
    var isLoadingHistory by remember { mutableStateOf(false) }

    // Load initial store history for selectedDate
    LaunchedEffect(selectedDate) {
        isLoadingHistory = true
        selectedDateSkins = onLoadHistoryForDate(selectedDate)
        isLoadingHistory = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B121A)),
            border = BorderStroke(1.2.dp, Color(0xFF2E3E52)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFFFF7A00),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OFFERS HISTORY CALENDAR",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Month Switcher Header
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF16222F),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { currentMonth = currentMonth.minusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    tint = Color.White
                                )
                            }

                            Text(
                                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase()} ${currentMonth.year}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = Color(0xFFFFD54F)
                            )

                            IconButton(
                                onClick = {
                                    if (currentMonth.isBefore(YearMonth.now(ZoneOffset.UTC))) {
                                        currentMonth = currentMonth.plusMonths(1)
                                    }
                                },
                                enabled = currentMonth.isBefore(YearMonth.now(ZoneOffset.UTC)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = if (currentMonth.isBefore(YearMonth.now(ZoneOffset.UTC))) Color.White else Color.Gray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of Week Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                        dayNames.forEach { name ->
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7E93),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Calendar Month Grid
                    CalendarMonthGrid(
                        month = currentMonth,
                        today = today,
                        selectedDate = selectedDate,
                        recordedDates = recordedDates,
                        onDateSelected = { date ->
                            selectedDate = date
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Selected Date Shop Header
                    val dateFormatted = remember(selectedDate) {
                        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH))
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF141D27),
                        border = BorderStroke(1.dp, Color(0xFF263545)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = dateFormatted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (selectedDate == today) "Today's Shop Rotation" else "Recorded Shop Archive",
                                        fontSize = 11.sp,
                                        color = if (selectedDate == today) Color(0xFFFF9800) else Color(0xFF8B9BB0)
                                    )
                                }

                                if (recordedDates.contains(selectedDate.toString())) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF261908),
                                        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🔥 ARCHIVED", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Skin Offers List / Cards
                            when {
                                isLoadingHistory -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color(0xFFFF4655),
                                            strokeWidth = 2.5.dp
                                        )
                                    }
                                }
                                selectedDateSkins != null && selectedDateSkins!!.isNotEmpty() -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        selectedDateSkins!!.forEach { skin ->
                                            CompactHistoricalSkinCard(skin = skin)
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F151C),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "📦", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No store recorded for this date",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "ValoMobile archives your shop every day you open the app.",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF7A8B9E),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222F3E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "BACK TO STORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    recordedDates: Set<String>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = month.lengthOfMonth()

    val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        for (week in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayIdx in 0 until 7) {
                    val cellIndex = week * 7 + dayIdx
                    val dayNumber = cellIndex - dayOfWeekOffset + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = month.atDay(dayNumber)
                        val dateStr = date.toString()
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        val isFuture = date.isAfter(today)
                        val hasHistory = recordedDates.contains(dateStr)

                        CalendarDayCell(
                            dayNumber = dayNumber,
                            isToday = isToday,
                            isSelected = isSelected,
                            isFuture = isFuture,
                            hasHistory = hasHistory,
                            onClick = {
                                if (!isFuture) onDateSelected(date)
                            }
                        )
                    } else {
                        // Empty spacer cell
                        Spacer(modifier = Modifier.size(38.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    hasHistory: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> Color(0xFFFF4655)
                    hasHistory -> Color(0xFF2E1C0A)
                    isToday -> Color(0xFF1E293B)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday || isSelected) 1.5.dp else if (hasHistory) 1.dp else 0.dp,
                color = when {
                    isSelected -> Color.White
                    isToday -> Color(0xFFFF9800)
                    hasHistory -> Color(0xFFFF9800).copy(alpha = 0.5f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !isFuture, onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                fontSize = 11.sp,
                fontWeight = if (isToday || isSelected || hasHistory) FontWeight.Black else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isFuture -> Color(0xFF4A5568).copy(alpha = 0.4f)
                    hasHistory -> Color(0xFFFFD54F)
                    isToday -> Color(0xFFFF9800)
                    else -> Color(0xFF9AA7B5)
                }
            )

            if (hasHistory) {
                Box(
                    modifier = Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0xFFFF7A00))
                )
            }
        }
    }
}

@Composable
private fun CompactHistoricalSkinCard(skin: SkinItem) {
    val numberFormat = remember { NumberFormat.getIntegerInstance(Locale.US) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF16212E),
        border = BorderStroke(1.dp, Color(0xFF263545)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weapon / Skin Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F161F),
                modifier = Modifier.size(width = 68.dp, height = 42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (skin.displayIcon.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(skin.displayIcon)
                                .crossfade(true)
                                .build(),
                            contentDescription = skin.displayName,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name & Weapon Type
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = skin.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = skin.weaponType,
                    fontSize = 10.sp,
                    color = Color(0xFF7A8B9E)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF0D141C),
                border = BorderStroke(1.dp, Color(0xFF2E3E52))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://media.valorant-api.com/currencies/85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741/displayicon.png")
                            .crossfade(true)
                            .build(),
                        contentDescription = "VP",
                        modifier = Modifier.size(12.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = numberFormat.format(skin.finalPrice),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekDayItem(day: WeekDayStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = day.dayName.take(3),
            fontSize = 11.sp,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (day.isToday) Color(0xFFFF9800) else Color(0xFF7A8B9E)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.isCompleted -> Brush.verticalGradient(
                            listOf(Color(0xFFFF9800), Color(0xFFFF5722))
                        )
                        day.isToday -> Brush.verticalGradient(
                            listOf(Color(0xFF2A1C12), Color(0xFF1E140C))
                        )
                        else -> Brush.verticalGradient(
                            listOf(Color(0xFF16212E), Color(0xFF111923))
                        )
                    }
                )
                .then(
                    if (day.isToday && !day.isCompleted) {
                        Modifier.border(1.5.dp, Color(0xFFFF9800), CircleShape)
                    } else if (day.isToday) {
                        Modifier.border(1.5.dp, Color.White, CircleShape)
                    } else {
                        Modifier.border(1.dp, Color(0xFF263545), CircleShape)
                    }
                )
        ) {
            if (day.isCompleted) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "${day.dayOfMonth}",
                    fontSize = 11.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (day.isToday) Color(0xFFFF9800) else Color(0xFF5B6B7D)
                )
            }
        }
    }
}
