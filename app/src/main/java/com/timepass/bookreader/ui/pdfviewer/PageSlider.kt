package com.timepass.bookreader.ui.pdfviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun PageSlider(
    modifier: Modifier = Modifier,
    offset: (Float) -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    val trackWidthDp = 120.dp
    val handleWidthDp = 20.dp

    val density = LocalDensity.current
    val maxOffsetPx = with(density) { (trackWidthDp - handleWidthDp).toPx() }
    val minOffsetPx = 0f
    Box(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(width = trackWidthDp, height = 2.dp)
                .background(color = MaterialTheme.colorScheme.tertiary)
                .align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(color = MaterialTheme.colorScheme.primary)
                .size(width = 20.dp, height = 40.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val previousOffset = offsetX
                        offsetX = (offsetX + delta).coerceIn(minOffsetPx, maxOffsetPx)

                        val effectiveDelta = offsetX - previousOffset
                        offset(effectiveDelta)
                    }
                ),
        )
    }
}