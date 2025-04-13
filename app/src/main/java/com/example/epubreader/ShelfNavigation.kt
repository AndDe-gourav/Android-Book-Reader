package com.example.epubreader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ShelfNavigation(
    bookDataViewModel: BookDataViewModel,
    modifier: Modifier = Modifier
) {
    val shelfNavigationItems = listOf("Recent", "Favourites", "To Read", "Collection", "Done Reading")

    val currentBookShelf by bookDataViewModel.currentBookShelf.collectAsState()

    Box(
        modifier = modifier
    ) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ){}
            Box(
                modifier = Modifier
                    .size(400.dp, 24.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        LazyRow(
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
        ) {
            items(shelfNavigationItems) { item ->

                val offset by animateIntOffsetAsState(
                    targetValue = if (currentBookShelf == item) IntOffset(0, 20) else IntOffset.Zero,
                    animationSpec = tween( durationMillis = 100, easing = FastOutSlowInEasing),
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .offset { offset }
                        .width(140.dp)
                        .height(36.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(8.dp),
                            clip = true,
                            spotColor = colorResource(R.color.shadow)
                        )
                        .clickable(
                            onClick = {
                                bookDataViewModel.changeCurrentBookShelf(item)
                            }
                        )
                ) {
                    Box {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.inverseSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

