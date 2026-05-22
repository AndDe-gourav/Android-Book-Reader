package com.timepass.bookreader.ui.aboutbook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.timepass.bookreader.R
import com.timepass.bookreader.ui.home.BookStateViewModel
import com.timepass.bookreader.ui.home.BookStatusIconRow
import com.timepass.bookreader.ui.home.CollectionViewModel
import com.timepass.bookreader.ui.home.LibraryViewModel
import java.io.File

@Composable
fun AboutBookScreen(
    onBack: () -> Unit,
    openEdit: () -> Unit,
    openPdf: (Long) -> Unit,
    bookStateViewModel: BookStateViewModel,
    libraryViewModel: LibraryViewModel,
    collectionViewModel: CollectionViewModel,
    modifier: Modifier = Modifier
) {
    val book by libraryViewModel.selectedBook.collectAsState()
    LazyColumn(
        contentPadding = PaddingValues(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.zIndex(0f)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            onClick = { },
                            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                painterResource(R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24),
                                contentDescription = "prev",
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .size(
                                170.dp,
                                280.dp
                            )
                            .clickable {
                                book?.let { book ->
                                    openPdf(book.bookId)
                                }
                            },
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = book?.coverImagePath?.let { File(it) }
                            ),
                            contentDescription = "Book_cover",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            onClick = { },
                            shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                painterResource(R.drawable.arrow_forward_24dp_000000_fill0_wght300_grad0_opsz24),
                                contentDescription = "next",
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = book?.title ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 2.dp)
                )
                Text(
                    text = "L__ ${book?.author ?: "not available"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 8.dp)
                )
            }
            BookStatusIconRow(
                openEdit = openEdit,
                selectedBook = book,
                bookStateViewModel = bookStateViewModel,
                collectionViewModel = collectionViewModel,
                onBookDeleted = {
                    libraryViewModel.deleteBook(book?.bookId!!)
                    onBack()
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val formate = book?.format ?: "not available"
                    val creator = book?.creator ?: "not available"

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Formate - ",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = formate,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Creator - ",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = creator,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseSurface,
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(top = 15.dp))
            }
        }
    }
}


