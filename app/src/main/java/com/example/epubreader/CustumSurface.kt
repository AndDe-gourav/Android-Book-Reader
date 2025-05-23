package com.example.epubreader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.epubreader.ui.theme.EPUBReaderTheme

@Composable
fun CanvasSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    shadowColor: Color = Color.Black,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier
        .fillMaxSize()
        .systemBarsPadding()
    ) {
        Canvas(
            modifier = Modifier
            .matchParentSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(top = 90.dp, start = 32.dp, bottom = 120.dp)
        ) {
            val canvasWidth: Float = size.width
            val canvasHeight: Float = size.height

            val topLeft = Offset(0f, 0f)
            val topRight = Offset(canvasWidth/2, 0f)
            val bottomLeft = Offset(0f, canvasHeight/2)
            val bottomRight = Offset(canvasWidth/2, canvasHeight/2)
            val sideTop = Offset(topRight.x +20, topRight.y +  60)
            val sideBottom = Offset(bottomRight.x +20, bottomRight.y - 10)

            drawRect(
                topLeft = Offset(0f + 20, 0f),
                size = size.copy(width = 50f, height = 2000f),
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Gray, Color.Black)
                ),
            )

            val path = Path().apply {
                moveTo(topRight.x,topRight.y)
                drawLine(color = Color.Gray, start = topLeft, end = bottomLeft, strokeWidth = 1f)
                drawLine(color = Color.Gray, start = bottomLeft, end = bottomRight, strokeWidth = 1f)
                drawLine(color = Color.Gray, start = bottomRight, end = topRight, strokeWidth = 1f)
                drawLine(color = Color.Gray, start = topRight, end = topLeft, strokeWidth = 1f)
                drawLine(color = Color.Gray, start = topRight, end = sideTop, strokeWidth = 1f)
                lineTo(sideTop.x, sideTop.y)
                drawLine(color = Color.Black, start = sideTop, end = sideBottom, strokeWidth = 3f, cap = StrokeCap.Round)
                lineTo(sideBottom.x, sideBottom.y)
                drawLine(color = Color.Gray, start = sideBottom, end = bottomRight, strokeWidth = 1f)
                lineTo(bottomRight.x, bottomRight.y)
            }

            drawPath(
                path = path,
                color = backgroundColor,
            )


        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart),
            content = content
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SurfacePreview() {
    EPUBReaderTheme {
        CanvasSurface(
            content =
                {
                    Image(
                        painter = painterResource(id = R.drawable.third),
                        contentDescription = "",
                        modifier = Modifier
                            .padding(start = 33.dp, top = 91.dp, )
                            .height(282.dp)
                            .width(180.dp)
                            .clip(shape = RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
        )
    }
}
