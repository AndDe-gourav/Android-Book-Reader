package com.timepass.bookreader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timepass.bookreader.R

@Composable
fun GeneralTopBar(
    onBackClicked: () -> Unit,
    titleText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = Color.White,
            onClick = onBackClicked,
            shape = RoundedCornerShape(topEnd = 20.dp),
            border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24),
                contentDescription = "Back",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(bottomStart = 20.dp),
            border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.tertiary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    GeneralTopBar(
        onBackClicked = {},
        titleText = "Home"
    )
}