package com.timepass.bookreader.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.timepass.bookreader.R
import com.timepass.bookreader.data.entity.BookEntity

@Composable
fun BottomBar(
    openPdf: (Long) -> Unit,
    openStats: () -> Unit,
    libraryViewModel: LibraryViewModel,
    selectedBook: BookEntity?,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth().padding(2.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    openStats()
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.analytics_40dp_000000_fill0_wght300_grad0_opsz40),
                    contentDescription = "Stats",
                )
                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(5.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        selectedBook?.let { book ->
                            openPdf(book.bookId)
                        }
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play_button),
                    contentDescription = "Read",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = "Read",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            PdfSelection(
                libraryViewModel = libraryViewModel,
                openPdf = openPdf
            )
        }
    }
}