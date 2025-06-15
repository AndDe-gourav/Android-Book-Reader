package com.example.epubreader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.epubreader.model.Book


@Composable
fun BookShelf(
    bookDataViewModel: BookDataViewModel,
    recentBooks: List<Book>,
    favouritesBooks: List<Book>,
    toReadBooks: List<Book>,
    particularCollection: List<List<Book>>,
    completedBooks: List<Book>,
    listOfCollections: List<String>,
    currentBookShelf: String,
    modifier: Modifier = Modifier
) {

    val groupedBooks = remember(currentBookShelf, recentBooks, favouritesBooks, toReadBooks, listOfCollections, completedBooks) {
        when (currentBookShelf) {
            "Recent" -> recentBooks.chunked(3)
            "Favourites" -> favouritesBooks.chunked(3)
            "To Read" -> toReadBooks.chunked(3)
            "Collection" -> emptyList()
            "Done Reading" -> completedBooks.chunked(3)
            else -> {
                recentBooks.chunked(3)
            }
        }
    }

    Column(
        modifier = modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_large))
    ) {
        LazyColumn(
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(top = 75.dp , bottom = 50.dp)
        ) {
            if ( currentBookShelf != "Collection") {
                if (groupedBooks.isEmpty()){
                    item {
                        Text(
                            text = "This Book Shelf is Empty",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(75.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black
                        )
                    }
                }
                items(
                    items = groupedBooks,
                ) { rowBooks ->
                    if (groupedBooks.indexOf(rowBooks) != 2 || currentBookShelf != "Recent") {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowBooks.forEach { book ->
                                        if (rowBooks.indexOf(book) != 2 || groupedBooks.indexOf(rowBooks) == 0 || currentBookShelf != "Recent") {
                                            Surface(
                                                color = Color.White,
                                                tonalElevation = 8.dp,
                                                shadowElevation = 16.dp,
                                                modifier = Modifier
                                                    .height(100.dp)
                                                    .width(65.dp)
                                                    .clickable(
                                                        onClick = {
                                                            bookDataViewModel.selectBook(book.uri)
                                                        }
                                                    )
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(
                                                        model = book.bookCover
                                                    ),
                                                    contentDescription = "Shelf Book",
                                                    contentScale = ContentScale.FillBounds,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    if (rowBooks.indexOf(book) == 2 && groupedBooks.indexOf(rowBooks) == 1 && currentBookShelf == "Recent") {
                                        Surface(
                                            color = Color.Transparent,
                                            modifier = Modifier.height(100.dp).width(60.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.books),
                                                contentDescription = "LoadMoreBooks",
                                                contentScale = ContentScale.FillBounds
                                            )
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                                MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                        )
                                    )
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = colorResource(id = R.color.shadow)
                            )
                        }
                    }
                }
            }else {
                bookDataViewModel.collectionToList()
                if (groupedBooks.isEmpty()) {
                    item {
                        Text(
                            text = "This Book Shelf is Empty",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(75.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black
                        )
                    }
                }
                items(
                    items = particularCollection,
                ){ rowBooks  ->
                    val index = particularCollection.indexOf(rowBooks)
                    Column {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(42.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp)
                        ) {
                            items(
                                items = rowBooks,
                            ) { book ->
                                Surface(
                                    color = Color.White,
                                    tonalElevation = 8.dp,
                                    shadowElevation = 16.dp,
                                    modifier = Modifier
                                        .height(100.dp)
                                        .width(65.dp)
                                        .clickable(
                                            onClick = {
                                                bookDataViewModel.selectBook(book.uri)
                                            }
                                        )
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = book.bookCover
                                        ),
                                        contentDescription = "Shelf Book",
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
                                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceContainerHigh,
                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                    )
                                )
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = colorResource(id = R.color.shadow)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 10.dp),
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = listOfCollections[index],
                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                                color = Color.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}
