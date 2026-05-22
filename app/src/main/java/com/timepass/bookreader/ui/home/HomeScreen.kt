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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.zIndex
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
    snackbarHostState: SnackbarHostState,
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

    Scaffold(
        topBar = {
            GeneralDrawerTopBar(
                text = "Home",
                libraryViewModel = libraryViewModel,
                modifier = Modifier.zIndex(1f)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        bottomBar = {
            BottomBar(
                openPdf = openPdf,
                openStats = openStats,
                libraryViewModel = libraryViewModel,
                selectedBook = selectedBook,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
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
                    modifier = Modifier.padding(vertical = 8.dp)
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

            item { Spacer(modifier = Modifier.size(100.dp)) }
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
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                HorizontalDivider(thickness = 0.5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFeedbackClick() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star_24dp_000000_fill0_wght300_grad0_opsz24),
                        contentDescription = "Feedback",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Give Feedback",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Help us improve the app",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLicenseClick() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Open Source Licenses",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Third-party library notices",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(580.dp)
        ) {
            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onBackground)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Open Source Licenses",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    item { MuPdfLicenseCard(context) }

                }
            }
        }
    }
}



@Composable
fun MuPdfLicenseCard(context: Context) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MuPDF",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Artifex Software, Inc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "AGPL-3.0",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Used for rendering and displaying PDF documents within the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
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
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
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
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "License Summary",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ad_group_24dp_000000_fill0_wght300_grad0_opsz24 else R.drawable.add_40dp_000000_fill0_wght300_grad0_opsz40
                    ),
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = MUPDF_LICENSE_SUMMARY,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}



private val MUPDF_LICENSE_SUMMARY = """
MuPDF is Copyright (C) 2006-2023 Artifex Software, Inc.
Licensed under the GNU Affero General Public License, version 3 (AGPL-3.0).

This application uses MuPDF to render PDF documents. Under the AGPL-3.0:

- You may use, study, share, and modify this software.
- If you distribute this app (modified or unmodified), you must make the
  complete corresponding source code available under the AGPL-3.0.
- If you run a modified version of the software as a network service,
  you must offer users the ability to receive the source code.
- This application's source code is available at:
  https://github.com/<your-repo-here>

The full AGPL-3.0 license text is available at:
https://www.gnu.org/licenses/agpl-3.0.html

For a commercial license that does not require open-source disclosure,
contact Artifex Software at: https://artifex.com/licensing/
""".trimIndent()