package com.example.epubreader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    bookURi: String,
    timeGoalViewModel: TimeGoalViewModel,
    year: Int,
    month: Int,
) {
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    var mapOfGoal by remember { mutableStateOf(mapOf<Int, Int>()) }
    val firstDayOfMonth = LocalDate.of(year, month, 1).dayOfWeek.value % 7
    val listOfDays = listOf("S","M", "T", "W", "T", "F", "S")

    LaunchedEffect(Unit){
        mapOfGoal = timeGoalViewModel.allTimeGoalBooksToMap(bookURi)
    }
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())} $year",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                    modifier = Modifier.height(200.dp)
            ) {
                items(listOfDays) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = it,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                items(firstDayOfMonth) {
                    Box(modifier = Modifier.fillMaxWidth())
                }

                items(daysInMonth) { index ->
                    val day = index + 1
                    DateCell(
                        mapOfGoal = mapOfGoal,
                        day = day
                    )
                }
            }
        }
    }
}

@Composable
fun DateCell(
    mapOfGoal: Map<Int, Int>,
    day: Int
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(color =
                if (day == LocalDate.now().dayOfMonth){
                    colorResource(id = R.color.TodayColor)
                }else {
                    if (mapOfGoal[day] == 1) {
                        colorResource(id = R.color.LightGreen)
                    } else if (mapOfGoal[day] == 0) {
                        colorResource(
                            id = R.color.LightRed
                        )
                    } else {
                        Color.White
                    }
                },
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            color = Color.Black
        )
    }
}
