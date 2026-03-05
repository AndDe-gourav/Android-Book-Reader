package com.example.bookReader.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookReader.R
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    pdfViewerViewModel: PdfViewerViewModel,
    statsViewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    var expandedItemIndex by remember { mutableStateOf<Int?>(null) }

    val booksWithStats by statsViewModel.booksWithStats.collectAsState()
    val isLoading by statsViewModel.isLoading.collectAsState()

    // Refresh whenever a session state changes (e.g. just ended a session)
    val sessionState by pdfViewerViewModel.sessionState.collectAsState()
    LaunchedEffect(sessionState) {
        if (sessionState == null) statsViewModel.refresh()
    }

    Box(modifier = modifier.fillMaxSize()) {
        GeneralTopBar(
            titleText = "Stats",
            onBackClicked = { navController.navigate("homeScreen") },
            modifier = Modifier.zIndex(1f)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxSize()
            ) {
                item { Spacer(modifier = Modifier.padding(40.dp)) }

                // Empty state
                item {
                    if (booksWithStats.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No reading goals set yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.inverseSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Set a daily reading goal while reading a book\nto track your progress here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                }

                itemsIndexed(items = booksWithStats) { index, entry ->
                    BookStatCard(
                        entry = entry,
                        isExpanded = expandedItemIndex == index,
                        statsViewModel = statsViewModel,
                        onToggleExpand = {
                            expandedItemIndex = if (expandedItemIndex == index) null else index
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Book stat card
// ---------------------------------------------------------------------------

@Composable
fun BookStatCard(
    entry: BookStatEntry,
    isExpanded: Boolean,
    statsViewModel: StatsViewModel,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTimeHours = entry.totalReadingTimeMs / 3_600_000L
    val totalTimeMinutes = (entry.totalReadingTimeMs / 60_000L) % 60

    val goalMinutes = entry.dailyGoalMinutes ?: 0
    val goalHours = goalMinutes / 60
    val goalMins = goalMinutes % 60

    Surface(
        color = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp,
        modifier = modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggleExpand
            )
    ) {
        Column {
            // ── Main info row ────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(
                    start = 12.dp, top = 12.dp, bottom = 12.dp, end = 2.dp
                )
            ) {
                // Book cover
                Surface(
                    color = Color.White,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(0.2f)
                        .shadow(
                            elevation = 4.dp,
                            shape = MaterialTheme.shapes.small,
                            spotColor = colorResource(R.color.shadow)
                        )
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = entry.book.coverImagePath?.let { File(it) }
                        ),
                        contentDescription = entry.book.title,
                        contentScale = ContentScale.FillBounds
                    )
                }

                // Title / author / progress
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = entry.book.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = entry.book.author ?: "Unknown Author",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    // Time done ↔ goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: time done
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (entry.isGoalMet) {
                                    "Goal Complete"
                                } else if (totalTimeHours > 0) {
                                    "${totalTimeHours}h ${totalTimeMinutes}m"
                                } else {
                                    "${totalTimeMinutes}m"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.inverseSurface
                            )
                            if (!entry.isGoalMet) {
                                Text(
                                    text = " Done",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.inverseSurface
                                )
                            }
                        }

                        // Right: goal
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (goalHours > 0) "${goalHours}h ${goalMins}m"
                                else "${goalMins}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.inverseSurface,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = " Goal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.inverseSurface
                            )
                        }
                    }

                    // Progress bar
                    CustumSlideBar(
                        value = entry.goalProgress,
                        color = if (entry.isGoalMet)
                            colorResource(id = R.color.CompleteBar)
                        else
                            MaterialTheme.colorScheme.outline
                    )
                }
            }

            // ── Expandable calendar ──────────────────────────────────────────
            AnimatedVisibility(visible = isExpanded) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    StatsCalendarView(
                        bookId = entry.book.bookId,
                        dailyGoalMinutes = entry.dailyGoalMinutes ?: 0,
                        statsViewModel = statsViewModel,
                        year = LocalDate.now().year,
                        month = LocalDate.now().monthValue
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Calendar view — per-day goal indicator
// ---------------------------------------------------------------------------

@Composable
fun StatsCalendarView(
    bookId: Long,
    dailyGoalMinutes: Int,
    statsViewModel: StatsViewModel,
    year: Int,
    month: Int
) {
    val coroutineScope = rememberCoroutineScope()
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(year, month, 1).dayOfWeek.value % 7
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    // Map of day-of-month → goal met (true/false). Absent days = no session.
    var goalMap by remember { mutableStateOf(mapOf<Int, Boolean>()) }

    LaunchedEffect(bookId, year, month) {
        coroutineScope.launch {
            goalMap = statsViewModel.getMonthlyGoalMap(
                bookId = bookId,
                year = year,
                month = month,
                dailyGoalMinutes = dailyGoalMinutes
            )
        }
    }

    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month + year header
            Text(
                text = "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())} $year",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(220.dp)
            ) {
                // Day-of-week headers
                items(dayLabels) { label ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Empty cells before the 1st
                items(firstDayOfMonth) {
                    Box(modifier = Modifier.fillMaxWidth())
                }

                // Day cells
                items(daysInMonth) { index ->
                    val day = index + 1
                    StatsDateCell(day = day, goalMet = goalMap[day])
                }
            }

            // Legend
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                LegendDot(
                    color = colorResource(id = R.color.LightGreen),
                    label = "Goal met"
                )
                LegendDot(
                    color = colorResource(id = R.color.LightRed),
                    label = "Goal missed"
                )
                LegendDot(
                    color = colorResource(id = R.color.TodayColor),
                    label = "Today"
                )
            }
        }
    }
}

@Composable
fun StatsDateCell(
    day: Int,
    goalMet: Boolean?   // null = no session recorded
) {
    val today = LocalDate.now().dayOfMonth
    val bgColor = when {
        day == today -> colorResource(id = R.color.TodayColor)
        goalMet == true -> colorResource(id = R.color.LightGreen)
        goalMet == false -> colorResource(id = R.color.LightRed)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(color = bgColor, shape = RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$day", color = Color.Black)
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth(0f)
                .background(color = color, shape = RoundedCornerShape(2.dp))
                .padding(horizontal = 6.dp)
        )
        Box(
            modifier = Modifier
                .height(12.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp))
                .padding(horizontal = 6.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )
    }
}