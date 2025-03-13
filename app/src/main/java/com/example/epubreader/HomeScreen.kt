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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
                            modifier = modifier.padding(bottom = 22.dp)
                        )
                    }
                )
            }
            BottomBar(
                navController = navController,
                bookDataViewModel = bookDataViewModel,
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
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
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
                            painter = painterResource(id = R.drawable.baseline_menu_book_24),
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
                Slider(
                    value = sliderProgress,
                    onValueChange = {},
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.outline,
                        activeTrackColor = MaterialTheme.colorScheme.outline,
                        inactiveTrackColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
                Text(
                    text = if (totalPages != 0) {
                        val percentage = (lastPage.toFloat() / totalPages * 100).toInt()
                        "$lastPage/$totalPages Completed ($percentage%)"
                    } else {
                        " Completed (0%)"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    modifier = Modifier.align(Alignment.End).padding(vertical = 2.dp),
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


    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        AnimatedIconButton(
            isActive = selectedBook?.favourite == 1,
            activeIcon = R.drawable.baseline_star_rate_24,
            inactiveIcon = R.drawable.baseline_star_border_24,
            contentDescription = "Favourites",
            onClick = {
                selectedBook?.let { bookDataViewModel.toggleFavorite(it) }
                snackBarContent(if (selectedBook?.favourite == 0)"Added to \"Favourites\" " else "Removed from \"Favourites\" ")
            }
        )

        // To Read icon
        AnimatedIconButton(
            isActive = selectedBook?.toRead  == 1,
            activeIcon = R.drawable.baseline_access_time_filled_24,
            inactiveIcon = R.drawable.baseline_access_time_24,
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
            isActive = selectedBook?.collection == "",
            activeIcon = R.drawable.baseline_folder_copy_24,
            inactiveIcon = R.drawable.baseline_folder_open_24,
            contentDescription = "Collection",
            onClick = {
            }
        )

        // Done Reading icon
        AnimatedIconButton(
            isActive = selectedBook?.doneReading == 1,
            activeIcon = R.drawable.baseline_done_24,
            inactiveIcon = R.drawable.baseline_done_outline_24,
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
            ){}
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
                                style = TextStyle( textDecoration = if (currentBookShelf == item) TextDecoration.Underline else TextDecoration.None,),
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
    val completedBooks by bookDataViewModel.completedBooks.collectAsState()

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
            items(
                items = groupedBooks,
                key = { row -> row.joinToString { it.uri } }
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
                painter = painterResource(R.drawable.dots),
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
                    bookDataViewModel.sharePdf( context = context, Uri.parse(selectedBook?.uri))
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
    text: String ,
    onCancelClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Snackbar(
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.outline,
        contentColor = Color.Black,
        action = {
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
                    painter = painterResource(id = R.drawable.baseline_menu_24),
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
                    painter = painterResource(id = R.drawable.baseline_search_24),
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
    modifier: Modifier = Modifier
) {
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    val encodedUri = Uri.encode(selectedBook?.uri)
    Box(
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 8.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bar_graph),
                        contentDescription = "Report",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.size(20.dp)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = if (false) painterResource(id = R.drawable.baseline_nightlight_round_24) else painterResource(id = R.drawable.baseline_sunny_24),
                        contentDescription = "light mode",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.clickable (onClick = { })
                    )
                    Text(
                        text = if (false) "Dark" else "Light",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseSurface
                    )
                }
            }
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
            painter = painterResource(id = R.drawable.baseline_home_24),
            text = "Home",
            currentScreen = currentScreen,
            label = "homeScreen",
            drawerState = drawerState
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.baseline_library_books_24),
            text = "Books",
            currentScreen = currentScreen,
            drawerState = drawerState

        )
        DrawerRow(
            painter = painterResource(id = R.drawable.baseline_people_24),
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
            painter = painterResource(id = R.drawable.baseline_settings_24),
            text = "Settings",
            currentScreen = currentScreen,
            drawerState = drawerState
        )
        DrawerRow(
            painter = painterResource(id = R.drawable.baseline_feedback_24),
            text = "Feedback",
            currentScreen = currentScreen,
            drawerState = drawerState
        )

    }
}

@Composable
fun DrawerRow(
    painter: Painter,
    text: String,
    label: String = "",
    currentScreen: String,
    drawerState: DrawerState,
    onDrawerItemClick: () -> Unit = {},
    modifier: Modifier = Modifier
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
                if (currentScreen == label){
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
                painter = painterResource(id = R.drawable.baseline_download_24),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewerScreen(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    pdfUri: String?
) {
    val context = LocalContext.current
    val uri = pdfUri?.let { Uri.parse(it) }

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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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




