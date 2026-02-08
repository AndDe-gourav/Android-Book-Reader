package com.example.bookReader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSelector(
    colorsList: List<Color>,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 7.dp)
    ){
        items(colorsList) { color ->
            Surface(
                border = BorderStroke(2.dp, Color.White),
                onClick = { onColorChange(color) },
                shape = RoundedCornerShape(8.dp),
                color = color,
                shadowElevation = 4.dp,
                modifier = Modifier.padding(5.dp).size(50.dp)
            ) {

            }
        }
    }
}