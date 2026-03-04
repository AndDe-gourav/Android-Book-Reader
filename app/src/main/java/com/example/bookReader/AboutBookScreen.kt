package com.example.bookReader

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookReader.ui.theme.BookStateViewModel
import com.example.bookReader.ui.theme.CollectionViewModel
import com.example.bookReader.ui.theme.GeneralTopBar
import com.example.bookReader.ui.theme.LibraryViewModel
import java.io.File

@Composable
fun AboutBookScreen(
    bookStateViewModel: BookStateViewModel ,
    libraryViewModel: LibraryViewModel,
    collectionViewModel: CollectionViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val book by libraryViewModel.selectedBook.collectAsState()
    Scaffold(
        topBar = {
            GeneralTopBar(
                titleText = "About Book",
                onBackClicked = { navController.popBackStack()},
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding).zIndex(0f)
        ) {
            item {
                Box(
                    modifier = Modifier
                ) {
                    Surface(
                        color = Color.White,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier
                            .size(
                                200.dp,
                                300.dp
                            )
                            .shadow(
                                elevation = 8.dp,
                                shape = MaterialTheme.shapes.small,
                                spotColor = colorResource(R.color.shadow)
                            )
                            .clickable {
                                book?.let { book ->
                                    navController.navigate("pdfReader/${book.bookId}")
                                }
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = book?.coverImagePath?.let { File(it) }
                            ),
                            contentDescription = "Book_cover_1",
                            contentScale = ContentScale.FillBounds
                        )
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
                        color = MaterialTheme.colorScheme.inverseSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 2.dp)
                    )
                    Text(
                        text = "L__ ${book?.author ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, end = 8.dp)
                    )
                }
                AnimatedIconRow(
                    selectedBook = book,
                    bookStateViewModel = bookStateViewModel,
                    collectionViewModel = collectionViewModel,
                    navController = navController,
                    onBookDeleted = {
                        libraryViewModel.deleteBook(book?.bookId!!)
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()

                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "time",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp, bottom = 1.dp)
                        )
                        Text(
                            text = "Last read time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseSurface,
                        )
                        Text(
                            text = "size",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp, bottom = 1.dp)
                        )
                        Text(
                            text = "File format and size",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseSurface,
                        )
                        Spacer(modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }
        }
    }
}


