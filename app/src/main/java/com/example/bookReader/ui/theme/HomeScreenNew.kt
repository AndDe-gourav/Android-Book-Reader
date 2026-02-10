package com.example.bookReader.ui.theme

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bookReader.CustumSlideBar
import com.example.bookReader.R
import com.example.bookReader.data.entity.BookEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenImproved(
    navController: NavController,
    toOpenDrawer: () -> Unit,
    toCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    bookStateViewModel: BookStateViewModel = hiltViewModel(),
    collectionViewModel: CollectionViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Collect state from ViewModels
    val allBooks by libraryViewModel.allBooks.collectAsState()
    val selectedBook by libraryViewModel.selectedBook.collectAsState()
    val currentBookShelf by libraryViewModel.currentBookShelf.collectAsState()

    // Book state collections
    val recentBooks by bookStateViewModel.recentBooks.collectAsState()
    val favoriteBooks by bookStateViewModel.favoriteBooks.collectAsState()
    val toReadBooks by bookStateViewModel.toReadBooks.collectAsState()
    val readingBooks by bookStateViewModel.readingBooks.collectAsState()
    val completedBooks by bookStateViewModel.completedBooks.collectAsState()

    val allCollections by collectionViewModel.allCollections.collectAsState()
    val snackbarMessage by libraryViewModel.snackbarMessage.collectAsState()

    // UI state
    var showAboutDocument by rememberSaveable { mutableStateOf(false) }

    val activity = context as? Activity
    var backPressCount by remember { mutableIntStateOf(0) }

    // Handle snackbar messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            val job = launch {
                snackbarHostState.showSnackbar(message = message)
            }
            delay(2000)
            job.cancel()
            libraryViewModel.clearSnackbar()
        }
    }

    BackHandler {
        backPressCount++
        when (backPressCount) {
            1 -> {
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
            2 -> {
                activity?.finish()
            }
        }
        coroutineScope.launch {
            delay(3000)
            backPressCount--
        }
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = showAboutDocument,
            label = "about_document_animation"
        ) { aboutDocument ->
            if (!aboutDocument) {
                Box(modifier = modifier.fillMaxSize()) {
                    // Top Bar
                    GeneralDrawerTopBar(
                        toOpenDrawer = toOpenDrawer,
                        text = "Home",
                        modifier = Modifier.zIndex(1f)
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { Spacer(modifier = Modifier.size(60.dp)) }

                        // Currently Reading Section
                        item {
                            CurrentlyReadingSection(
                                selectedBook = selectedBook,
                                bookStateViewModel = bookStateViewModel,
                                onBookClick = { book ->
                                    libraryViewModel.selectBook(book)
                                    showAboutDocument = true
                                },
                                onFavoriteClick = { book, isFavorite ->
                                    bookStateViewModel.updateBookState(
                                        bookId = book.bookId,
                                        isFavorite = !isFavorite
                                    )
                                }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = colorResource(id = R.color.progress_bar_front_color)
                            )
                        }

                        // Shelf Navigation
                        item {
                            ShelfNavigationSection(
                                currentShelf = currentBookShelf,
                                collections = allCollections,
                                onShelfSelected = { shelfType ->
                                    libraryViewModel.changeBookShelf(shelfType)
                                },
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        // Books based on current shelf
                        item {
                            val booksToDisplay = when (currentBookShelf) {
                                is BookShelfType.Recent -> recentBooks
                                is BookShelfType.Favorites -> favoriteBooks
                                is BookShelfType.ToRead -> toReadBooks
                                is BookShelfType.Completed -> completedBooks
                                is BookShelfType.Collection -> allBooks // TODO: Filter by collection
                            }

                            BookShelfSection(
                                books = booksToDisplay,
                                onBookClick = { book ->
                                    libraryViewModel.selectBook(book)
                                },
                                onBookLongClick = { book ->
                                    libraryViewModel.selectBook(book)
                                    showAboutDocument = true
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.size(100.dp)) }
                    }

                    // Snackbar
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 90.dp)
                    ) { snackBarData ->
                        CustomSnackBar(
                            text = snackBarData.visuals.message,
                            onCancelClicked = { snackBarData.dismiss() }
                        )
                    }

                    // Bottom Bar
                    BottomBar(
                        libraryViewModel = libraryViewModel,
                        bookStateViewModel = bookStateViewModel,
                        navController = navController,
                        toCloseDrawer = toCloseDrawer,
                        selectedBook = selectedBook,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            } else {
                AboutDocumentScreen(
                    book = selectedBook,
                    bookStateViewModel = bookStateViewModel,
                    onBackClick = { showAboutDocument = false },
                    onNavigateToReader = { book ->
                        navController.navigate("pdfReader/${book.bookId}")
                    },
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun CurrentlyReadingSection(
    selectedBook: BookEntity?,
    bookStateViewModel: BookStateViewModel,
    onBookClick: (BookEntity) -> Unit,
    onFavoriteClick: (BookEntity, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var bookState by remember { mutableStateOf<com.example.bookReader.data.entity.BookStateEntity?>(null) }

    LaunchedEffect(selectedBook ) {
        selectedBook?.let {
            bookState = bookStateViewModel.getBookState(it.bookId)
        }
    }
    Column {
        Row(
            modifier = modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
        ) {
            Surface(
                color = Color.White,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .size(
                        dimensionResource(id = R.dimen.book_cover_width),
                        dimensionResource(id = R.dimen.book_cover_height)
                    )
                    .shadow(
                        elevation = 4.dp,
                        shape = MaterialTheme.shapes.small,
                        spotColor = colorResource(R.color.shadow)
                    )

            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = selectedBook?.title
                    ),
                    contentDescription = "Book_cover_1",
                    contentScale = ContentScale.FillBounds,
                )
            }
            Column(
                modifier = Modifier.padding( top = 2.dp, start = 16.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    selectedBook?.title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.book),
                            contentDescription = "Read",
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    onClick = {
                                        onBookClick(selectedBook!!)
                                    }
                                ),
                        )
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseSurface
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.size(20.dp)
                )
                CustumSlideBar(
                    value = bookState?.currentPage?.toFloat() ?: 0.0f,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (selectedBook?.totalPages != 0) {
                        val percentage = (bookState?.currentPage?.div(selectedBook?.totalPages!!)
                            ?.times(100))
                        "${bookState?.currentPage}/${selectedBook?.totalPages} Completed ($percentage%)"
                    } else {
                        " Completed (0%)"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.inverseSurface
                )
            }
        }

    }
}

@Composable
fun ShelfNavigationSection(
    currentShelf: BookShelfType,
    collections: List<com.example.bookReader.data.entity.CollectionEntity>,
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
                text = "Completed",
                isSelected = currentShelf is BookShelfType.Completed,
                onClick = { onShelfSelected(BookShelfType.Completed) }
            )
        }

        items(collections) { collection ->
            ShelfChip(
                text = collection.name,
                isSelected = currentShelf is BookShelfType.Collection &&
                        currentShelf.collectionId == collection.collectionId,
                onClick = { onShelfSelected(BookShelfType.Collection(collection.collectionId)) }
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
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier
    )
}

@Composable
fun BookShelfSection(
    books: List<BookEntity>,
    onBookClick: (BookEntity) -> Unit,
    onBookLongClick: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (books.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No books in this shelf",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                books.forEach { book ->
                    Surface(
                        color = Color.White,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .height(100.dp)
                            .width(65.dp)
                            .clickable{
                                onBookClick(book)
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = book.title
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
        }
    }
}

@Composable
fun CustomSnackBar(
    modifier: Modifier = Modifier,
    text: String,
    onCancelClicked: () -> Unit = {},
) {
    Snackbar(
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.outline,
        contentColor = Color.Black,
        action = {
            if (!text.contains("Collection")) {
                TextButton(onClick = onCancelClicked) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorResource(id = R.color.shadow),
                        letterSpacing = 2.sp
                    )
                }
            }
        },
        modifier = modifier.padding(horizontal = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(id = R.color.text_color),
        )
    }
}

@Composable
fun GeneralDrawerTopBar(
    toOpenDrawer: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable { toOpenDrawer() },
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            shadowElevation = 4.dp,
        ) {
            IconButton(onClick = { toOpenDrawer() }) {
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.TopEnd),
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            shadowElevation = 4.dp,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 13.dp, horizontal = 34.dp)
            )
        }
    }
}

@Composable
fun BottomBar(
    libraryViewModel: LibraryViewModel,
    bookStateViewModel: BookStateViewModel,
    navController: NavController,
    toCloseDrawer: () -> Unit,
    selectedBook: BookEntity?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(80.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter)
        ) {}
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .align(Alignment.BottomCenter)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navController.navigate("StatsScreen") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.chart),
                        contentDescription = "Stats",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        selectedBook?.let { book ->
                            navController.navigate("pdfReader/${book.bookId}")
                        }
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play_button),
                        contentDescription = "Read",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text(
                    text = "Read",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.inverseSurface
                )
            }
            QuickPdfSelection(
                libraryViewModel = libraryViewModel,
                navController = navController,
                toCloseDrawer = toCloseDrawer
            )
        }
    }
}

@Composable
fun QuickPdfSelection(
    libraryViewModel: LibraryViewModel,
    navController: NavController,
    toCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Take persistable permission
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)

            // Extract PDF metadata and add to library
            coroutineScope.launch {
                val metadata = PdfUtil.extractPdfMetadata(context, it)
                metadata?.let { meta ->
                    val bookId = libraryViewModel.addBook(
                        title = meta.title,
                        author = meta.author,
                        uri = it,
                        totalPages = meta.totalPages
                    )

                    if (bookId != -1L) {
                        // Navigate to PDF reader
                        navController.navigate("pdfReader/$bookId")
                    }
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(40.dp)
                .offset(y = (-14).dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add_round),
                contentDescription = "Add new Book",
                tint = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier
                    .clickable {
                        pdfLauncher.launch(arrayOf("application/pdf"))
                        toCloseDrawer()
                    }
                    .padding(4.dp)
            )
        }
        Text(
            text = "Add Book",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.inverseSurface,
            modifier = Modifier.offset(y = (-4).dp)
        )
    }
}

@Composable
fun AboutDocumentScreen(
    book: BookEntity?,
    bookStateViewModel: BookStateViewModel,
    onBackClick: () -> Unit,
    onNavigateToReader: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.zIndex(0f)
    ) {
        item {
            Spacer(modifier = Modifier.padding(top = 80.dp))
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
                            if (book?.uri != null) {
                            }
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = book?.title
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