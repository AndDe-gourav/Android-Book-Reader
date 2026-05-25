package com.timepass.bookreader.ui.home

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.timepass.bookreader.R
import com.timepass.bookreader.ui.InfoOptionsDialog
import com.timepass.bookreader.ui.LicenseScreen
import com.timepass.bookreader.ui.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    openPdf: (Long) -> Unit,
    openStats: () -> Unit,
    openAbout: () -> Unit,
    openEdit: () -> Unit,
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel,
    bookStateViewModel: BookStateViewModel,
    collectionViewModel: CollectionViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val selectedBook by libraryViewModel.selectedBook.collectAsState()
    val currentBookShelf by libraryViewModel.currentBookShelf.collectAsState()

    val recentBooks by bookStateViewModel.recentBooks.collectAsState()
    val favoriteBooks by bookStateViewModel.favoriteBooks.collectAsState()
    val toReadBooks by bookStateViewModel.toReadBooks.collectAsState()
    val completedBooks by bookStateViewModel.completedBooks.collectAsState()

    val collectionsWithBooks by collectionViewModel.allCollectionsWithBooks.collectAsState()

    val activity = context as? Activity
    var backPressCount by remember { mutableIntStateOf(0) }

    BackHandler {
        backPressCount++
        when (backPressCount) {
            1 -> Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            2 -> activity?.finish()
        }
        coroutineScope.launch {
            delay(3000)
            backPressCount--
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            CurrentlyReadingCard(
                selectedBook = selectedBook,
                bookStateViewModel = bookStateViewModel,
                onBookClick = { book ->
                    libraryViewModel.selectBook(book)
                    openAbout()
                },
            )
            BookStatusIconRow(
                openEdit = openEdit,
                selectedBook = selectedBook,
                bookStateViewModel = bookStateViewModel,
                collectionViewModel = collectionViewModel,
                onBookDeleted = {
                    libraryViewModel.deleteBook(selectedBook?.bookId!!)
                },
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = colorResource(id = R.color.progress_bar_front_color)
            )
        }

        item {
            ShelfNavigationSection(
                currentShelf = currentBookShelf,
                onShelfSelected = { shelfType ->
                    libraryViewModel.changeBookShelf(shelfType)
                },
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        item {
            if (currentBookShelf is BookShelfType.Collection) {
                CollectionShelfSection(
                    collectionsWithBooks = collectionsWithBooks,
                    onBookClick = { book -> libraryViewModel.selectBook(book) }
                )
            } else {
                val booksToDisplay = when (currentBookShelf) {
                    is BookShelfType.Recent -> recentBooks
                    is BookShelfType.Favorites -> favoriteBooks
                    is BookShelfType.ToRead -> toReadBooks
                    is BookShelfType.Completed -> completedBooks
                    else -> emptyList()
                }
                BookShelfSection(
                    books = booksToDisplay,
                    onBookClick = { book -> libraryViewModel.selectBook(book) },
                )
            }
        }
    }
}

@Composable
fun ShelfNavigationSection(
    currentShelf: BookShelfType,
    onShelfSelected: (BookShelfType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ShelfChip(
                text = "Recent",
                isSelected = currentShelf is BookShelfType.Recent,
                onClick = { onShelfSelected(BookShelfType.Recent) }
            )
        }
        item {
            ShelfChip(
                text = "Favorites",
                isSelected = currentShelf is BookShelfType.Favorites,
                onClick = { onShelfSelected(BookShelfType.Favorites) }
            )
        }
        item {
            ShelfChip(
                text = "To Read",
                isSelected = currentShelf is BookShelfType.ToRead,
                onClick = { onShelfSelected(BookShelfType.ToRead) }
            )
        }
        item {
            ShelfChip(
                text = "Collection",
                isSelected = currentShelf is BookShelfType.Collection,
                onClick = { onShelfSelected(BookShelfType.Collection) }
            )
        }
        item {
            ShelfChip(
                text = "Completed",
                isSelected = currentShelf is BookShelfType.Completed,
                onClick = { onShelfSelected(BookShelfType.Completed) }
            )
        }

    }
}

@Composable
fun ShelfChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        selected = isSelected,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(5.dp)
        )
    }
}

@Composable
fun PdfSelection(
    libraryViewModel: LibraryViewModel,
    openPdf: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)

            coroutineScope.launch {
                val bookId = libraryViewModel.importPdf( context, it )

                if (bookId != -1L) {
                    openPdf(bookId)
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { pdfLauncher.launch(arrayOf("application/pdf")) }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add_40dp_000000_fill0_wght300_grad0_opsz40),
                contentDescription = "Add new Book",
            )
        }
        Text(
            text = "Add Book",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(5.dp)
        )
    }
}



@Composable
fun GeneralDrawerTopBar(
    text: String,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }
    var showLicenseScreen by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        InfoOptionsDialog(
            onDismiss = { showInfoDialog = false },
            onFeedbackClick = {
                showInfoDialog = false
                libraryViewModel.onFeedBackIconClicked(context)
            },
            onLicenseClick = {
                showInfoDialog = false
                showLicenseScreen = true
            }
        )
    }

    if (showLicenseScreen) {
        LicenseScreen(onDismiss = { showLicenseScreen = false })
    }

    TopBar(
        onActionClicked = { showInfoDialog = !showInfoDialog },
        titleText = text,
        icon = R.drawable.contact_support_24dp_000000_fill0_wght300_grad0_opsz24
    )
}