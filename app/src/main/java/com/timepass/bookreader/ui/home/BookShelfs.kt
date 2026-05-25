package com.timepass.bookreader.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.timepass.bookreader.R
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.entity.CollectionWithBooks
import java.io.File

@Composable
fun BookShelfSection(
    books: List<BookEntity>,
    onBookClick: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (books.isEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No books in this shelf",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        Column {
            books.chunked(3).forEach { rowBooks ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowBooks.forEach { book ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .width(70.dp)
                                .aspectRatio(2f / 3f)
                                .clickable { onBookClick(book) }
                        ) {
                            AsyncImage(
                                model = book.coverImagePath,
                                contentDescription = "Book Cover",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(color = colorResource(R.color.book_shelf_2))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorResource(R.color.book_shelf_1),
                                    colorResource(R.color.book_shelf_3)
                                )
                            )
                        )
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = colorResource(id = R.color.shadow)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CollectionShelfSection(
    collectionsWithBooks: List<CollectionWithBooks>,
    onBookClick: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val nonEmpty = collectionsWithBooks.filter { it.books.isNotEmpty() }

    if (nonEmpty.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No books in any collection yet.\nTap the folder icon on a book to add it.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(modifier = modifier) {
        nonEmpty.forEach { cwb ->
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cwb.books) { book ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .height(100.dp)
                            .width(65.dp)
                            .clickable { onBookClick(book) }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = book.coverImagePath?.let { File(it) }
                            ),
                            contentDescription = book.title,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = colorResource(R.color.book_shelf_2))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.book_shelf_1),
                                colorResource(R.color.book_shelf_3)
                            )
                        )
                    )
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = colorResource(id = R.color.shadow)
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Text(
                    text = cwb.collection.name,
                    modifier = Modifier.padding(vertical = 3.dp, horizontal = 10.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}