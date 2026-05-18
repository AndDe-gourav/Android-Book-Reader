package com.timepass.bookreader.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.entity.BookStateEntity
import java.io.File

@Composable
fun CurrentlyReadingCard(
    selectedBook: BookEntity?,
    bookStateViewModel: BookStateViewModel,
    onBookClick: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var bookState by remember {
        mutableStateOf<BookStateEntity?>(
            null
        )
    }

    LaunchedEffect(selectedBook) {
        selectedBook?.let {
            bookState = bookStateViewModel.getBookState(it.bookId)
        }
    }

    val targetProgress = remember(bookState, selectedBook) {
        val currentPage = bookState?.currentPage?.toFloat() ?: 0f
        val totalPages = selectedBook?.totalPages?.toFloat() ?: 1f
        if (totalPages > 0) (currentPage / totalPages).coerceIn(0f, 1f) else 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )

    val percentage = remember(bookState, selectedBook) {
        val currentPage = bookState?.currentPage ?: 0
        val totalPages = selectedBook?.totalPages ?: 0
        if (totalPages > 0) ((currentPage.toFloat() / totalPages.toFloat()) * 100).toInt() else 0
    }

    Column {
        Row(
            modifier = modifier
                .padding(15.dp)
                .clickable { selectedBook?.let { onBookClick(it) } }
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .size( 80.dp, 120.dp )
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = selectedBook?.coverImagePath?.let { File(it) }
                    ),
                    contentDescription = "Book_cover",
                    contentScale = ContentScale.FillBounds,
                )
            }

            Column(
                modifier = Modifier.padding(top = 2.dp, start = 16.dp, end = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    selectedBook?.title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(20.dp))
                ProgressBar(
                    value = animatedProgress,
                    frontColor = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${bookState?.currentPage ?: 0}/${selectedBook?.totalPages ?: 0} • $percentage% Complete",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    value: Float,
    frontColor: Color,
    backColor: Color = MaterialTheme.colorScheme.surface,
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    color = backColor,
                ),
        )
        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(value)
                .padding(3.dp)
                .background(
                    color = frontColor,
                ),
        )
    }
}
