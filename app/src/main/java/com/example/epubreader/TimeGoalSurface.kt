package com.example.epubreader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TimeGoalSurface(
    currentTimeInSec: Int,
    timeGoal: Int,
    onEditClicked: () -> Unit ,
    modifier: Modifier = Modifier
) {
    val currentTimeHours = currentTimeInSec/3600
    val currentTimeMinutes = (currentTimeInSec%3600)/60
    val currentTimeSeconds = currentTimeInSec%60
    val currentTimeSecondString: String = if (currentTimeSeconds < 10 ){
        "0$currentTimeSeconds"
    }else{
        currentTimeSeconds.toString()
    }
    val currentTimeMinutesString: String = if (currentTimeMinutes < 10 ){
        "0$currentTimeMinutes"
    }else{
        currentTimeMinutes.toString()
    }


    val timeGoalHours = timeGoal/60
    val timeGoalMinutes = timeGoal%60

    Surface(
        color = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp,
        modifier = modifier.padding(horizontal = 90.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text =
                    if (currentTimeMinutes== 0){"00:00:$currentTimeSecondString"}
                else if(currentTimeHours == 0){"00:${currentTimeMinutesString}:$currentTimeSecondString"}
                else {"$currentTimeHours:$currentTimeMinutesString:$currentTimeSecondString"}
                ,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "-----",
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text =
                    if(timeGoalHours == 0){"${timeGoalMinutes}m"}
                else{"${timeGoalHours}h ${timeGoalMinutes}m"},
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.outline)
                    .height(1.dp)
                    .width(130.dp)
            )
            TextButton(
                onClick = {
                    onEditClicked()
                }
            ) {
                Text(
                    text = "Edit Time Goal",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}