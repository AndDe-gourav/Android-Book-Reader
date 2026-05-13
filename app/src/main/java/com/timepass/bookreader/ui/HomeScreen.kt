package com.timepass.bookreader.ui

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.timepass.bookreader.R
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.entity.CollectionWithBooks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomeScreen(
    navController: NavController,
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
        modifier = modifier,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                item {
                    CurrentlyReadingSection(
                        selectedBook = selectedBook,
                        bookStateViewModel = bookStateViewModel,
                        onBookClick = { book ->
                            libraryViewModel.selectBook(book)
                            navController.navigate("AboutBookScreen")
                        },
                    )
                    AnimatedIconRow(
                        selectedBook = selectedBook,
                        bookStateViewModel = bookStateViewModel,
                        collectionViewModel = collectionViewModel,
                        navController = navController,
                        onBookDeleted = {
                            libraryViewModel.deleteBook(selectedBook?.bookId!!)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
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

            BottomBar(
                libraryViewModel = libraryViewModel,
                navController = navController,
                selectedBook = selectedBook,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
                        color = Color.White,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
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
                modifier = Modifier.padding(start = 12.dp),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = cwb.collection.name,
                    modifier = Modifier.padding(vertical = 3.dp, horizontal = 10.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BookShelfSection(
    books: List<BookEntity>,
    onBookClick: (BookEntity) -> Unit,
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
            books.chunked(3).forEach { rowBooks ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowBooks.forEach { book ->
                        Surface(
                            color = Color.White,
                            tonalElevation = 8.dp,
                            shadowElevation = 16.dp,
                            modifier = Modifier
                                .height(100.dp)
                                .width(65.dp)
                                .clickable { onBookClick(book) }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = book.coverImagePath?.let { File(it) }
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
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


@Composable
fun CurrentlyReadingSection(
    selectedBook: BookEntity?,
    bookStateViewModel: BookStateViewModel,
    onBookClick: (BookEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var bookState by remember { mutableStateOf<com.timepass.bookreader.data.entity.BookStateEntity?>(null) }

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
                .padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
                .clickable { selectedBook?.let { onBookClick(it) } }
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
                    ),
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = selectedBook?.coverImagePath?.let { File(it) }
                    ),
                    contentDescription = "Book_cover_1",
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
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(20.dp))
                CustumSlideBar(
                    value = animatedProgress,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${bookState?.currentPage ?: 0}/${selectedBook?.totalPages ?: 0} • $percentage% Complete",
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
        item {
            ShelfChip(
                text = "Collection",
                isSelected = currentShelf is BookShelfType.Collection,
                onClick = { onShelfSelected(BookShelfType.Collection) }
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
fun CustumSlideBar(
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(value)
                .padding(3.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(4.dp)
                )

        )
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .align(Alignment.CenterEnd)
                .size(3.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
fun BottomBar(
    libraryViewModel: LibraryViewModel,
    navController: NavController,
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
            PdfSelection(
                libraryViewModel = libraryViewModel,
                navController = navController,
            )
        }
    }
}

@Composable
fun PdfSelection(
    libraryViewModel: LibraryViewModel,
    navController: NavController,
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
                    .clickable { pdfLauncher.launch(arrayOf("application/pdf")) }
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            shadowElevation = 4.dp,
        ) {
            IconButton(
                onClick = { showInfoDialog = true }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info_alt),
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            shadowElevation = 4.dp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.inverseSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 13.dp, horizontal = 34.dp)
            )
        }
    }
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
                        painter = painterResource(id = R.drawable.comment),
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
fun MuPdfLicenseCard(context: android.content.Context) {
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
                        id = if (expanded) R.drawable.expand_down else R.drawable.expand_up
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