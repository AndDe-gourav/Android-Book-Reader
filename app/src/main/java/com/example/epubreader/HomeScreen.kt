package com.example.epubreader

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    currentScreen: String,
    drawerState: DrawerState,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val selectedBook by bookDataViewModel.selectedBook.collectAsState()

    Column(
        modifier = modifier
    ) {
        TopBar(
            drawerState = drawerState,
            scope = CoroutineScope(coroutineScope.coroutineContext)
        )
        BookInReading(
            bookDataViewModel = bookDataViewModel,
            navController = navController,
            currentScreen = currentScreen,
            snackBarContent = { message  ->
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    val job = launch {
                        snackbarHostState.showSnackbar(message = message)
                    }
                    delay(2000)
                    job.cancel() 
                }
            }
        )
        HorizontalDivider(
            thickness = (0.5).dp,
            color = colorResource(id = R.color.progress_bar_front_color)
        )
        Box {
            ShelfNavigation(
                modifier = Modifier.zIndex(1f),
                bookDataViewModel = bookDataViewModel
            )
            Column(
                modifier = Modifier
                    .zIndex(0f)
            ) {
                BookShelf(
                    bookDataViewModel = bookDataViewModel,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackBarData ->
                        SnackBar(
                            text = snackBarData.visuals.message,
                            onCancelClicked = {
                                snackBarData.dismiss()
                                when (snackBarData.visuals.message){
                                    "Added to \"Favourites\" " -> selectedBook?.let { bookDataViewModel.toggleFavorite(it) }
                                    "Added to \"To Read\" " -> selectedBook?.let { bookDataViewModel.toggleToRead(it) }
                                    "Added to \"Done Reading\" " -> selectedBook?.let { bookDataViewModel.toggleDoneReading(it) }
                                    "Removed from \"Favourites\" " -> selectedBook?.let { bookDataViewModel.toggleFavorite(it) }
                                    "Removed from \"To Read\" " -> selectedBook?.let { bookDataViewModel.toggleToRead(it) }
                                    "Removed from \"Done Reading\" " -> selectedBook?.let { bookDataViewModel.toggleDoneReading(it) }
                                }
                            },
                            modifier = modifier.padding(bottom = 28.dp)
                        )
                    }
                )
            }
            BottomBar(
                navController = navController,
                bookDataViewModel = bookDataViewModel,
                drawerState = drawerState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun BookInReading(
    bookDataViewModel: BookDataViewModel,
    snackBarContent: (String) -> Unit,
    navController: NavController,
    currentScreen: String,
    modifier: Modifier = Modifier
) {
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()

    LaunchedEffect(selectedBook) {
        selectedBook?.let { bookDataViewModel.fetchTotalPages(it.uri) }
        selectedBook?.let { bookDataViewModel.fetchLastPage(it.uri) }
    }

    val lastPage by bookDataViewModel.lastPage.collectAsState()
    val totalPages by bookDataViewModel.totalPages.collectAsState()

    val sliderProgress by animateFloatAsState(
        targetValue = if (totalPages != 0) lastPage.toFloat() / totalPages else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "Slider Animation"
    )

    Column {
        Row(
            modifier = modifier.padding(
                top = dimensionResource(id = R.dimen.padding_medium),
                start = dimensionResource(id = R.dimen.padding_medium),
                bottom = dimensionResource(id = R.dimen.padding_small)
            )
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
                        elevation = 8.dp,
                        shape = MaterialTheme.shapes.small,
                        spotColor = colorResource(R.color.shadow)
                    )
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = selectedBook?.bookCover
                    ),
                    contentDescription = "Book_cover_1",
                    contentScale = ContentScale.FillBounds
                )
            }
            Column(
                modifier = Modifier.padding(
                    top = dimensionResource(id = R.dimen.padding_very_small),
                    start = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LazyColumn{
                        item{
                            selectedBook?.let {
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.inverseSurface,
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {

                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.book),
                            contentDescription = "Read",
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier
                                .size(26.dp)
                                .clickable(
                                    onClick = {
                                        navController.navigate("aboutDocument")
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
                    value = sliderProgress
                )
                Text(
                    text = if (totalPages != 0) {
                        val percentage = (lastPage.toFloat() / totalPages * 100).toInt()
                        "$lastPage/$totalPages Completed ($percentage%)"
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
        AnimatedIconRow(
            bookDataViewModel = bookDataViewModel,
            navController = navController,
            snackBarContent = snackBarContent,
            currentScreen = currentScreen
        )
    }
}

@Composable
fun AnimatedIconRow(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    currentScreen: String,
    snackBarContent: (String) -> Unit ,
    ) {
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    val listOfCollections by bookDataViewModel.listOfCollections.collectAsState()


    var collectionValue by remember { mutableStateOf("") }
    var openNewCollectionDialog by remember { mutableStateOf(false) }
    var openCollectionDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        AnimatedIconButton(
            isActive = selectedBook?.favourite == 1,
            activeIcon = R.drawable.star_fill,
            inactiveIcon = R.drawable.star,
            contentDescription = "Favourites",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleFavorite(it) }
                snackBarContent(if (selectedBook?.favourite == 0)"Added to \"Favourites\" " else "Removed from \"Favourites\" ")
            }
        )

        // To Read icon
        AnimatedIconButton(
            isActive = selectedBook?.toRead  == 1,
            activeIcon = R.drawable.clock_fill,
            inactiveIcon = R.drawable.clock,
            contentDescription = "To Read",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleToRead(it) }
                if (selectedBook?.doneReading == 1){
                    selectedBook?.let { bookDataViewModel.toggleDoneReading(it) }
                }
                snackBarContent(if (selectedBook?.toRead == 0) "Added to \"To Read\" " else "Removed from \"To Read\" ")
            }
        )

        // Collection icon
        AnimatedIconButton(
            isActive = selectedBook?.collection != "",
            activeIcon = R.drawable.folder_dublicate_fill,
            inactiveIcon = R.drawable.folder_dublicate,
            contentDescription = "Collection",
            onClick = {
                bookDataViewModel.collectionToList()
                openCollectionDialog = true
            }
        )

        // Done Reading icon
        AnimatedIconButton(
            isActive = selectedBook?.doneReading == 1,
            activeIcon = R.drawable.check_round_fill,
            inactiveIcon = R.drawable.check_ring_round,
            contentDescription = "Done Reading",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleDoneReading(it) }
                if (selectedBook?.toRead == 1){
                    selectedBook?.let { bookDataViewModel.toggleToRead(it) }
                }
                snackBarContent(if (selectedBook?.doneReading == 0) "Added to \"Done Reading\" " else "Removed from \"Done Reading\" ")
            }
        )

        OptionsDropDownMenu(
            bookDataViewModel = bookDataViewModel,
            navController = navController,
            currentScreen = currentScreen
        )

        if (openNewCollectionDialog) {
            OnNotInCollectionsIconClicked(
                value = collectionValue,
                onValueChange = { collectionValue = it },
                onDismiss = {
                    collectionValue = ""
                    openNewCollectionDialog = false
                },
                onCreateClicked = {
                    if (!listOfCollections.map { it.lowercase() }.contains(collectionValue.lowercase())) {
                        selectedBook?.let {
                            bookDataViewModel.updateCollection(it.uri, remove = "", collectionValue)
                        }
                        coroutineScope.launch {
                            delay(300)
                            bookDataViewModel.collectionToList()
                        }
                        snackBarContent("Added to Collection \n\"$collectionValue\" ")
                        collectionValue = ""
                        openNewCollectionDialog = false
                        openCollectionDialog = false

                    } else {
                        snackBarContent("Collection already exists")
                        collectionValue = ""
                        openNewCollectionDialog = false
                        openCollectionDialog = false
                    }
                },

                )
        }

        if (openCollectionDialog) {
            OnInCollectionIconClicked(
                bookDataViewModel = bookDataViewModel,
                onDismiss = {
                    bookDataViewModel.collectionToList()
                    openCollectionDialog = false
                            },
                onCreateNewClicked = { openNewCollectionDialog = true },
                snackBarContent = snackBarContent
            )
        }

    }
}

@Composable
fun AnimatedIconButton(
    isActive: Boolean,
    activeIcon: Int,
    inactiveIcon: Int,
    contentDescription: String,
    onClick: () -> Unit
) {

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    IconButton(
        onClick = {
            isPressed = true
            onClick()
        }
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }

        Icon(
            painter = painterResource(if (isActive) activeIcon else inactiveIcon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.inverseSurface,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
fun ShelfNavigation(
    bookDataViewModel: BookDataViewModel,
    modifier: Modifier = Modifier
) {
    val shelfNavigationItems = listOf("Recent", "Favourites", "To Read", "Collection", "Done Reading")

    val currentBookShelf by bookDataViewModel.currentBookShelf.collectAsState()

    Box(
        modifier = modifier
    ) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ){}
            Box(
                modifier = Modifier
                    .size(400.dp, 24.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        LazyRow(
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_large)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
        ) {
            items(shelfNavigationItems) { item ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .width(140.dp)
                        .height(36.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(8.dp),
                            clip = true,
                            spotColor = colorResource(R.color.shadow)
                        )
                        .clickable(
                            onClick = {
                                bookDataViewModel.changeCurrentBookShelf(item)
                            }
                        )
                ) {
                    Box {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            ) {
                            Text(
                                text = item,
                                style = TextStyle(textDecoration = if (currentBookShelf == item) TextDecoration.Underline else TextDecoration.None),
                                color = MaterialTheme.colorScheme.inverseSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BookShelf(
    bookDataViewModel: BookDataViewModel,
    modifier: Modifier = Modifier
) {
    val recentBooks by bookDataViewModel.allBooks.collectAsState()
    val favouritesBooks by bookDataViewModel.favoriteBooks.collectAsState()
    val toReadBooks by bookDataViewModel.toReadBooks.collectAsState()
    val particularCollection by bookDataViewModel.particularCollection.collectAsState()
    val completedBooks by bookDataViewModel.completedBooks.collectAsState()
    val listOfCollections by bookDataViewModel.listOfCollections.collectAsState()

    val currentBookShelf by bookDataViewModel.currentBookShelf.collectAsState()


    val groupedBooks = when(currentBookShelf){
        "Recent" -> recentBooks.chunked(3)
        "Favourites" -> favouritesBooks.chunked(3)
        "To Read" -> toReadBooks.chunked(3)
        "Collection" -> emptyList()
        "Done Reading" -> completedBooks.chunked(3)
        else -> {
            recentBooks.chunked(3)
        }
    }


    Column(
        modifier = modifier
            .padding(vertical = dimensionResource(id = R.dimen.padding_large))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(top = 75.dp , bottom = 50.dp)
        ) {
            if ( currentBookShelf != "Collection") {
                items(
                    items = groupedBooks,
                ) { rowBooks ->
                    Column {
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
                    }
                }
            }else {
                bookDataViewModel.collectionToList()
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


@Composable
fun OptionsDropDownMenu(
    navController: NavController,
    modifier: Modifier = Modifier,
    currentScreen: String,
    bookDataViewModel: BookDataViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    val lastOpenedBook by bookDataViewModel.lastOpenedBook.collectAsState()

    var openRemoveDialog by remember { mutableStateOf(false) }

    if (openRemoveDialog) {
        RemovePopUp(
            onDismiss = { openRemoveDialog = false },
            onRemoveClicked = {
                selectedBook?.let { bookDataViewModel.deleteBook(it) }
                openRemoveDialog = false
                if (currentScreen == "aboutDocument"){
                    navController.navigateUp()
                }
                              },
        )
    }

    Box(
        modifier = modifier
    ) {
        IconButton(
            onClick = {
                expanded = true
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.meatballs_menu),
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.size(24.dp),
            )
        }
        DropdownMenu(
            containerColor = MaterialTheme.colorScheme.onBackground,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Edit", style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    navController.navigate("EditScreen")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Share",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = {
                    selectedBook?.uri?.let { bookDataViewModel.sharePdf( context = context, it.toUri()) }
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Remove",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = {
                    expanded = false
                    openRemoveDialog = true
                }
            )
        }
    }
}


@Composable
fun SnackBar(
    modifier: Modifier = Modifier,
    text: String ,
    onCancelClicked: () -> Unit = {},
) {
    Snackbar(
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.outline,
        contentColor = Color.Black,
        action = {
            if(!text.contains("Collection")) {
                TextButton(
                    onClick = onCancelClicked
                ) {
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
fun TopBar(
    drawerState: DrawerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.TopStart),
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            shadowElevation = 4.dp,
            ) {
            IconButton(
                onClick = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            ) {
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
            shadowElevation = 4.dp
        ) {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search_alt),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(24.dp)
                )

            }
        }
    }
}


@Composable
fun BottomBar(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    drawerState: DrawerState,
    modifier: Modifier = Modifier
) {
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    val encodedUri = Uri.encode(selectedBook?.uri)
    Box(
        modifier = modifier.height(80.dp)
    ) {
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chart),
                    contentDescription = "Report",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(25.dp)
                )
                Text(
                    text = "Report",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseSurface

                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = {
                        selectedBook?.let {
                            bookDataViewModel.updateBookTime(it)
                        }
                        navController.navigate("BookScreen/${encodedUri}")
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play_button),
                        contentDescription = "Read",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier
                            .size(35.dp)
                    )
                }
                Text(
                    text = "Read",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.inverseSurface
                )
            }
            QuickPdfSelection(
                bookDataViewModel = bookDataViewModel,
                navController = navController,
                drawerState = drawerState,
            )
        }
    }
}

@Composable
fun Drawer(
    navController: NavController,
    bookDataViewModel: BookDataViewModel,
    drawerState: DrawerState,
    currentScreen: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.applogo),
                contentDescription = "App Logo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(50.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
            )
            Spacer(
                modifier.width(dimensionResource(id = R.dimen.padding_large))
            )
            Text(
                text = "EPUB Reader",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        DrawerRow(
            painter = painterResource(id = R.drawable.home),
            text = "Home",
            currentScreen = currentScreen,
            label = "homeScreen",
            drawerState = drawerState
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.notebook),
            text = "Books",
            currentScreen = currentScreen,
            drawerState = drawerState

        )
        DrawerRow(
            painter = painterResource(id = R.drawable.group),
            text = "Authors",
            currentScreen = currentScreen,
            drawerState = drawerState

        )
        PDFSelection(
            navController = navController,
            bookDataViewModel = bookDataViewModel,
            drawerState = drawerState
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.setting_line),
            text = "Settings",
            currentScreen = currentScreen,
            drawerState = drawerState
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.comment),
            text = "Feedback",
            currentScreen = currentScreen,
            drawerState = drawerState
        )

    }
}

@Composable
fun DrawerRow(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    label: String = "",
    currentScreen: String,
    drawerState: DrawerState,
    onDrawerItemClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                width = 0.5.dp,
                color = if (currentScreen == label) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onDrawerItemClick()
                if (currentScreen == label) {
                    scope.launch {
                        drawerState.close()
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painter,
                contentDescription = "Navigators",
                tint = if (currentScreen == label) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.inverseSurface
            )
            Spacer(
                modifier.width(dimensionResource(id = R.dimen.padding_large))
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (currentScreen == label) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.inverseSurface
            )
        }
    }
}

@Composable
fun CustumSlideBar(
    value: Float,
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
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(value)
                .padding(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(4.dp)
                )

        )
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .align(Alignment.CenterEnd)
                .size(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
fun PDFSelection(
    navController: NavController,
    bookDataViewModel: BookDataViewModel,
    drawerState: DrawerState
) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val scope = rememberCoroutineScope()

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {

            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(it, takeFlags)

            val encodedUri = Uri.encode(it.toString())
            bookDataViewModel.addBook(it)
            navController.navigate("BookScreen/$encodedUri")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(
                onClick = {
                    pdfLauncher.launch(arrayOf("application/pdf"))
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.load_circle_fill),
                contentDescription = "Home",
            )
            Spacer(
                Modifier.width(dimensionResource(id = R.dimen.padding_large))
            )
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun QuickPdfSelection(
    navController: NavController,
    bookDataViewModel: BookDataViewModel,
    drawerState: DrawerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val scope = rememberCoroutineScope()

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {

            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(it, takeFlags)

            val encodedUri = Uri.encode(it.toString())
            bookDataViewModel.addBook(it)
            navController.navigate("BookScreen/$encodedUri")
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
                    .clickable(
                        onClick = {
                            pdfLauncher.launch(arrayOf("application/pdf"))
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewerScreen(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    pdfUri: String?
) {
    val context = LocalContext.current
    val uri = pdfUri?.toUri()

    val lastOpenedPageDB by bookDataViewModel.lastPage.collectAsState()
    var lastOpenedPageL by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (pdfUri != null) {
            bookDataViewModel.fetchLastPage(pdfUri)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            pdfUri?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    bookDataViewModel.updateLastPage(pdfUri, lastOpenedPageL)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("PDF Viewer") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            modifier = Modifier.zIndex(1f)
        )
        AndroidView(
            factory = { ctx -> PDFView(ctx, null) },
            update = { pdfView ->
                pdfView.fromUri(uri)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(lastOpenedPageDB)
                    .enableAnnotationRendering(false)
                    .scrollHandle(DefaultScrollHandle(context))
                    .spacing(10)
                    .nightMode(false)
                    .pageSnap(true)
                    .onPageChange { page, _ ->
                        lastOpenedPageL = page
                    }
                    .load()

            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

