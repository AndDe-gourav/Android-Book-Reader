package com.example.epubreader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun PdfBottomBar(
    modifier: Modifier = Modifier,
    onThemeClicked: () -> Unit = {},
    onLockClicked: () -> Unit = {},
    onTimerClicked: () -> Unit = {},
) {
     Surface(
         color = MaterialTheme.colorScheme.surfaceContainerHigh,
         shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
         shadowElevation = 4.dp,
         modifier = modifier
         ) {
         Row(
             modifier = Modifier.padding(horizontal = 12.dp),
             horizontalArrangement = Arrangement.spacedBy(12.dp)
         ) {
             IconButton(
                 onClick = { onThemeClicked() }
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.paint_roller),
                     contentDescription = "ColorPicker",
                     modifier = Modifier
                         .padding(3.dp)
                         .size(25.dp)
                 )
             }
             IconButton(
                 onClick = { onLockClicked() }
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.vector),
                     contentDescription = "LockHorizontalMovement",
                     modifier = Modifier
                         .padding(3.dp)
                         .size(25.dp)
                 )
             }
             IconButton(
                 onClick = { onTimerClicked() }
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.fluent_timer_12_regular),
                     contentDescription = "Timer",
                     modifier = Modifier
                         .padding(3.dp)
                         .size(25.dp)
                 )
             }
         }
     }
}


@Composable
fun PdfTopBar(
    onBackClicked: () -> Unit,
    onTocClicked: () -> Unit,
    onSortClicked: () -> Unit,
    onOptionsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable { onBackClicked() },
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            shadowElevation = 4.dp,
        ) {
            IconButton(
                onClick = {onBackClicked()}
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sign_out_circle),
                    contentDescription = "exit",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { onTocClicked() }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sort),
                        contentDescription = "TableOfContent",
                        modifier = Modifier
                            .padding(3.dp)
                            .size(25.dp)
                    )
                }
                IconButton(
                    onClick = {  }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.setting_line),
                        contentDescription = "Settings",
                        modifier = Modifier
                            .padding(3.dp)
                            .size(25.dp)
                    )
                }
                IconButton(
                    onClick = {  }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.meatballs_menu),
                        contentDescription = "Options",
                        modifier = Modifier
                            .padding(3.dp)
                            .size(25.dp)
                    )
                }
            }
        }
    }
}