package com.timepass.bookreader.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.timepass.bookreader.R
import com.timepass.bookreader.ui.TopBar
import com.timepass.bookreader.ui.pdfviewer.PdfUtil
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
                val metadata = PdfUtil.extractPdfMetadata(context, it)
                metadata?.let { meta ->
                    val bookId = libraryViewModel.addBook(
                        title = meta.title,
                        author = meta.author,
                        uri = it,
                        coverImagePath = meta.coverImagePath,
                        totalPages = meta.totalPages,
                        creator = meta.creator,
                        format = meta.format
                    )
                    if (bookId != -1L) {
                        libraryViewModel.restoreLastOpenedBook()
                        openPdf(bookId)
                    }
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



@Composable
fun InfoOptionsDialog(
    onDismiss: () -> Unit,
    onFeedbackClick: () -> Unit,
    onLicenseClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onFeedbackClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                    ) {
                        Text(
                            text = "Give Feedback",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Help us improve the app",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onLicenseClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp)
                    ) {
                        Text(
                            text = "Open Source Licenses",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Third-party library notices",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun LicenseScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        MuPdfLicenseCard(context = context)
    }
}

@Composable
fun MuPdfLicenseCard(context: Context) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MuPDF",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "Artifex Software, Inc.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(5.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Text(
                        text = "AGPL-3.0",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }

            Text(
                text = "Used for rendering and displaying PDF documents within the app.",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://git.ghostscript.com/?p=mupdf.git".toUri()
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "View Source",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(5.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://artifex.com/licensing/".toUri()
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Artifex Licensing",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "License Summary",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(5.dp)
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = MUPDF_LICENSE_SUMMARY,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    modifier = Modifier.padding(5.dp)
                )
            }
        }
    }
}



private val MUPDF_LICENSE_SUMMARY = """
This application uses MuPDF by Artifex Software, Inc.

MuPDF is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).

Under the AGPL, if you distribute this application or modified versions of MuPDF,
you must make the complete corresponding source code available under the same license.

Source code for this application is available at:
https://github.com/AndDe-gourav/Android-Book-Reader

MuPDF source code:
https://git.ghostscript.com/?p=mupdf.git

Full AGPL-3.0 license:
https://www.gnu.org/licenses/agpl-3.0.html

Commercial licensing for MuPDF is available from Artifex Software:
https://artifex.com/licensing/
""".trimIndent()