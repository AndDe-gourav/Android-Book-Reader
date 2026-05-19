package com.timepass.bookreader.ui.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.timepass.bookreader.R
import com.timepass.bookreader.ui.TopBar
import com.timepass.bookreader.ui.home.ProgressBar
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
    statsViewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    var expandedItemIndex by remember { mutableStateOf<Int?>(null) }

    val booksWithStats by statsViewModel.booksWithStats.collectAsState()
    val isLoading by statsViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                titleText = "Stats",
                onActionClicked = { navController.popBackStack() },
                icon = R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (isLoading) {
            // FIX 3: CircularProgressIndicator was not centered — added Box + fillMaxSize
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    if (booksWithStats.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
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


@Composable
fun BookStatCard(
    entry: BookStatEntry,
    isExpanded: Boolean,
    statsViewModel: StatsViewModel,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTimeHours = entry.todayReadingTimeMs / 3_600_000L
    val totalTimeMinutes = (entry.todayReadingTimeMs / 60_000L) % 60

    val goalMinutes = entry.dailyGoalMinutes ?: 0
    val goalHours = goalMinutes / 60
    val goalMins = goalMinutes % 60

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggleExpand
            )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    start = 12.dp, top = 12.dp, bottom = 12.dp, end = 2.dp
                )
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(0.2f)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = entry.book.coverImagePath?.let { File(it) }
                        ),
                        contentDescription = entry.book.title,
                        contentScale = ContentScale.FillBounds
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = entry.book.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = entry.book.author ?: "Unknown Author",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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

                    ProgressBar(
                        value = entry.goalProgress,
                        frontColor = if (entry.isGoalMet)
                            colorResource(id = R.color.CompleteBar)
                        else
                            MaterialTheme.colorScheme.outline,
                        backColor = MaterialTheme.colorScheme.background
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatsCalendarView(
                        bookId = entry.book.bookId,
                        statsViewModel = statsViewModel,
                        year = LocalDate.now().year,
                        month = LocalDate.now().monthValue
                    )
                }
            }
        }
    }
}


@Composable
fun StatsCalendarView(
    bookId: Long,
    statsViewModel: StatsViewModel,
    year: Int,
    month: Int
) {
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(year, month, 1).dayOfWeek.value % 7
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    // FIX 1: single state — all three maps set atomically, zero resets between them
    var monthlyStats by remember { mutableStateOf<MonthlyStats?>(null) }

    LaunchedEffect(bookId, year, month) {
        monthlyStats = statsViewModel.getMonthlyStats(bookId, year, month)
    }

    val goalMap     = monthlyStats?.goalMap     ?: emptyMap()
    val timeReadMap = monthlyStats?.timeReadMap ?: emptyMap()
    val goalSetMap  = monthlyStats?.goalSetMap  ?: emptyMap()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())} $year",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(220.dp)
            ) {
                items(dayLabels) { label ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label, fontWeight = FontWeight.SemiBold)
                    }
                }

                items(firstDayOfMonth) {
                    Box(modifier = Modifier.fillMaxWidth())
                }

                items(daysInMonth) { index ->
                    val day = index + 1
                    StatsDateCell(
                        day = day,
                        goalMet = goalMap[day],
                        timeRead = timeReadMap[day],
                        goalSet = goalSetMap[day],
                        year = year,
                        month = month,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                LegendDot(color = colorResource(id = R.color.LightGreen), label = "Goal met")
                LegendDot(color = colorResource(id = R.color.LightRed), label = "Goal missed")
                LegendDot(color = colorResource(id = R.color.TodayColor), label = "Today")
            }
        }
    }
}


@Composable
fun StatsDateCell(
    day: Int,
    goalMet: Boolean?,
    timeRead: Long?,
    goalSet: Int?,
    year: Int,
    month: Int,
) {
    val today = LocalDate.now()
    val isToday = day == today.dayOfMonth && year == today.year && month == today.monthValue

    // FIX 2: determine if this cell date is in the past or today (future dates not tappable)
    val cellDate = LocalDate.of(year, month, day)
    val isPastOrToday = !cellDate.isAfter(today)

    val bgColor = when {
        isToday -> colorResource(id = R.color.TodayColor)
        goalMet == true -> colorResource(id = R.color.LightGreen)
        goalMet == false -> colorResource(id = R.color.LightRed)
        else -> Color.White
    }

    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(color = bgColor)
            // FIX 2: was `if (goalMet != null)` — missed days with no goal record at all
            .clickable(enabled = isPastOrToday) {
                showMenu = true
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$day", color = Color.Black)

        DropdownMenu(
            shape = RoundedCornerShape(0.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "$day ${Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black,
                    )
                },
                onClick = {},
                enabled = false
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val goalText = if (goalSet != null) "${goalSet}m goal" else "no goal"
                        Text(text = goalText, fontSize = 13.sp)
                    }
                },
                onClick = { showMenu = false }
            )

            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val readText = when {
                            timeRead == null || timeRead == 0L -> "No reading recorded"
                            timeRead < 60 -> "${timeRead}m read"
                            else -> "${timeRead / 60}h ${timeRead % 60}m read"
                        }
                        Text(text = readText, fontSize = 13.sp)
                    }
                },
                onClick = { showMenu = false }
            )
        }
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
                .background(color = color)
                .padding(horizontal = 6.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface  // FIX 4: was Color.Black
        )
    }
}