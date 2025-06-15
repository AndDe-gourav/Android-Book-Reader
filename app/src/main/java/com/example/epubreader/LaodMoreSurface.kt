package com.example.epubreader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.epubreader.ui.theme.EPUBReaderTheme

@Composable
fun LoadMoreSurface(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            modifier = Modifier
                .height(80.dp)
                .width(15.dp)
        ){
            Image(
                painter = painterResource(R.drawable.whatsapp_image_2025_06_13_at_10_29_09_pm),
                contentDescription = "bookSideImage",
                modifier = Modifier.fillMaxSize()
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 16.dp,
            modifier = Modifier
                .height(94.dp)
                .width(12.dp)
        ){
            Image(
                painter = painterResource(id = R.drawable.whatsapp_image_2025_06_13_at_10_29_08_pm__1_),
                contentDescription = "bookSideImage",
                modifier = Modifier.fillMaxSize()
            )

        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .height(55.dp)
                .width(20.dp)
        ){
            Image(
                painter = painterResource(R.drawable.whatsapp_image_2025_06_13_at_10_29_09_pm),
                contentDescription = "bookSideImage",
                modifier = Modifier.fillMaxSize()
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    EPUBReaderTheme {
        LoadMoreSurface(
            modifier = Modifier.padding(12.dp)
        )
    }
}