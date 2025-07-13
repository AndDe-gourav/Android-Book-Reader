package com.example.epubreader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PdfBottomBar(
    isColorPaletteVisible: Boolean,
    isTimerVisible: Boolean,
    isHorizontalLocked: Boolean,
    modifier: Modifier = Modifier,
    isTimerEnabled: Boolean,
    onThemeClicked: () -> Unit = {},
    onLockClicked: () -> Unit = {},
    onTimerClicked: () -> Unit = {},
) {
     Surface(
         color = colorResource( R.color.Book),
         shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
         shadowElevation = 4.dp,
         modifier = modifier
         ) {
         Row(
             modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
             horizontalArrangement = Arrangement.spacedBy(12.dp)
         ) {
             FilledIconButton(
                 shape = RoundedCornerShape(8.dp),
                 colors = IconButtonDefaults.iconButtonColors(
                     if (isColorPaletteVisible) MaterialTheme.colorScheme.surfaceContainerHigh else colorResource(id = R.color.Book)
                 ),
                 onClick = { onThemeClicked()}
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.paint_roller),
                     contentDescription = "ColorPicker",
                     modifier = Modifier
                         .padding(3.dp)
                         .size(25.dp),
                     tint = Color.Black
                 )
             }

             FilledIconButton(
                 shape = RoundedCornerShape(8.dp),
                 colors = IconButtonDefaults.iconButtonColors(
                     if (isHorizontalLocked) MaterialTheme.colorScheme.surfaceContainerHigh else colorResource(id = R.color.Book)
                 ),
                 onClick = { onLockClicked() }
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.vector_1_),
                     contentDescription = "LockHorizontalMovement",
                     modifier = Modifier
                         .padding(3.dp)
                         .size(25.dp),
                     tint = Color.Black
                 )
             }
             FilledIconButton(
                 shape = RoundedCornerShape(8.dp),
                 colors = IconButtonDefaults.iconButtonColors(
                     if (isTimerVisible) MaterialTheme.colorScheme.surfaceContainerHigh else colorResource(id = R.color.Book)
                 ),
                 enabled = isTimerEnabled,
                 onClick = { onTimerClicked() }
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.fluent_timer_12_regular),
                     contentDescription = "Timer",
                     modifier = Modifier
                         .padding(3.dp)
                         .size(25.dp),
                     tint = if (isTimerEnabled)Color.Black else Color.Gray
                 )
             }
         }
     }
}


@Composable
fun PdfTopBar(
    isTocSheetVisible: Boolean,
    navController: NavController,
    isSystemUIVisible: Boolean,
    bookDataViewModel: BookDataViewModel,
    onBackClicked: () -> Unit,
    onTocClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = isSystemUIVisible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth }
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }
            ),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Surface(
                color = colorResource(R.color.Book),
                modifier = Modifier
                    .clickable { onBackClicked() },
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                shadowElevation = 4.dp,
            ) {
                IconButton(
                    onClick = { onBackClicked() }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sign_out_circle),
                        contentDescription = "exit",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = isSystemUIVisible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Surface(
                color = colorResource(R.color.Book),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledIconButton(
                        shape = RoundedCornerShape(8.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            if (isTocSheetVisible) MaterialTheme.colorScheme.surfaceContainerHigh else colorResource(
                                id = R.color.Book
                            )
                        ),
                        onClick = { onTocClicked() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sort),
                            contentDescription = "TableOfContent",
                            modifier = Modifier
                                .padding(3.dp)
                                .size(25.dp),
                            tint = Color.Black
                        )
                    }
                    PdfOptions(
                        bookDataViewModel = bookDataViewModel,
                        navController = navController,
                    )
                }
            }
        }
    }
}