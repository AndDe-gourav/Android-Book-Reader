package com.example.bookReader.ui

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
    statsViewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    var expandedItemIndex by remember { mutableStateOf<Int?>(null) }

    val booksWithStats by statsViewModel.booksWithStats.collectAsState()
    val isLoading by statsViewModel.isLoading.collectAsState()

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
            Row(
                modifier = Modifier.padding(
                    start = 12.dp, top = 12.dp, bottom = 12.dp, end = 2.dp
                )
            ) {
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

                    CustumSlideBar(
                        value = entry.goalProgress,
                        color = if (entry.isGoalMet)
                            colorResource(id = R.color.CompleteBar)
                        else
                            MaterialTheme.colorScheme.outline
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
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
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
    val coroutineScope = rememberCoroutineScope()
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(year, month, 1).dayOfWeek.value % 7
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    var goalMap by remember { mutableStateOf(mapOf<Int, Boolean>()) }

    LaunchedEffect(bookId, year, month) {
        coroutineScope.launch {
            goalMap = statsViewModel.getMonthlyGoalMap(
                bookId = bookId,
                year = year,
                month = month,
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
                    StatsDateCell(day = day, goalMet = goalMap[day], year = year, month = month)
                }
            }

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
    goalMet: Boolean?,
    year: Int,
    month: Int
) {
    val today = LocalDate.now()
    val isToday = day == today.dayOfMonth && year == today.year && month == today.monthValue
    val bgColor = when {
        isToday -> colorResource(id = R.color.TodayColor)
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
        // BUG FIX #10: The old composable had TWO Boxes — the first used fillMaxWidth(0f)
        // making it completely invisible (zero width). Only one dot Box is needed.
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