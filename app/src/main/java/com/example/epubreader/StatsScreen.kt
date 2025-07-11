package com.example.epubreader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

@Composable
fun StatsScreen(
    bookDataViewModel: BookDataViewModel,
    timeGoalViewModel: TimeGoalViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val timeGoalsBooks by timeGoalViewModel.allTimeGoalBooks.collectAsState()

    Box(
        modifier = modifier
    ) {
        GerenalTopBar(
            titleText = "Stats",
            onBackClicked = {
                navController.navigate("homeScreen")
            },
            modifier = Modifier.zIndex(1f)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.zIndex(0f)
        ) {
            item { Spacer(modifier = Modifier.padding(40.dp))  }
            items(
                items = timeGoalsBooks,
            ){ book ->

                val totalTimeHours = book.totalTime/3600000
                val totalTimeMinutes = (book.totalTime/60000)%60

                val timeGoalHours = book.timeGoal/60
                val timeGoalMinutes = book.timeGoal%60

                var bookTitle by remember(book) { mutableStateOf("Untitled") }
                var bookAuthor by remember(book) { mutableStateOf("Unknown Author") }
                var bookCover: String? by remember(book) { mutableStateOf("bookCover") }

                LaunchedEffect(book) {
                    bookTitle = bookDataViewModel.getBookFromUri(book.uri)?.title.toString()
                    bookAuthor = bookDataViewModel.getBookFromUri(book.uri)?.author.toString()
                    bookCover = bookDataViewModel.getBookFromUri(book.uri)?.bookCover
                }

                Surface(
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 2.dp)
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
                                    model = bookCover
                                ),
                                contentDescription = "Book_cover_1",
                                contentScale = ContentScale.FillBounds,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = bookTitle,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.inverseSurface,
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Text(
                                text = bookAuthor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Spacer(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if(book.goalCompleted == 0){ if(totalTimeHours != 0L){ totalTimeHours.toString()  +"h " + totalTimeMinutes.toString() + "m "}else { totalTimeMinutes.toString() + "m "}}else{ "Goal Complete"},
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                        )
                                        Text(
                                            text = if (book.goalCompleted == 0){"Done"}else{ ""},
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (timeGoalHours != 0){ timeGoalHours.toString()  +"h " + timeGoalMinutes.toString() + "m "}else { timeGoalMinutes.toString() + "m " },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                            textAlign = TextAlign.End,
                                        )
                                        Text(
                                            text = "Time Goal",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                            textAlign = TextAlign.End,
                                        )
                                    }
                                }
                                CustumSlideBar(
                                    value = (((book.totalTime/60000)/(book.timeGoal).toFloat())),
                                    color = colorResource(id= R.color.CompleteBar)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

