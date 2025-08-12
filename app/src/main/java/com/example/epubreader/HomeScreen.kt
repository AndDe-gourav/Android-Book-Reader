package com.example.epubreader

import android.Manifest
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.epubreader.model.bookStorage.Book
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    context: Context,
    bookDataViewModel: BookDataViewModel,
    timeGoalViewModel: TimeGoalViewModel,
    navController: NavController,
    toOpenDrawer: () -> Unit,
    toCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val postNotificationPermission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    val notificationHandler = NotificationHandler(context)

    LaunchedEffect(key1 = true) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier.systemBarsPadding().zIndex(1f)
    ) {
        Button(onClick = {
            notificationHandler.showNotification()
        }) { Text(text = "Simple notification") }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val recentBooks by bookDataViewModel.allBooks.collectAsState()
    val favouritesBooks by bookDataViewModel.favoriteBooks.collectAsState()
    val toReadBooks by bookDataViewModel.toReadBooks.collectAsState()
    val particularCollection by bookDataViewModel.particularCollection.collectAsState()
    val completedBooks by bookDataViewModel.completedBooks.collectAsState()
    val listOfCollections by bookDataViewModel.listOfCollections.collectAsState()
    val selectedBook by bookDataViewModel.selectedBook.collectAsState()
    val currentBookShelf by bookDataViewModel.currentBookShelf.collectAsState()

    var showAboutDocument by rememberSaveable{ mutableStateOf(false) }


    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var backPressCount by remember { mutableStateOf(0) }

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
        scope.launch {
            delay(3000)
            backPressCount--
        }
    }
    SharedTransitionLayout {
        AnimatedContent(
            targetState = showAboutDocument,
        ) { aboutDocument ->
            if (!aboutDocument) {
                Box(
                    modifier = modifier.fillMaxSize()
                ) {
                    GernealDrawerTopBar(
                        toOpenDrawer = toOpenDrawer,
                        text = "Home",
                        modifier = Modifier.zIndex(1f)
                    )
                    LazyColumn(
                        modifier = Modifier
                    ) {
                        item {
                            Spacer(
                                modifier = Modifier.size(60.dp)
                            )
                            BookInReading(
                                bookDataViewModel = bookDataViewModel,
                                timeGoalViewModel = timeGoalViewModel,
                                showAboutDocument = showAboutDocument,
                                onAboutDocumentClicked = { showAboutDocument = true },
                                navController = navController,
                                selectedBook = selectedBook,
                                listOfCollections = listOfCollections,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@AnimatedContent,
                                snackBarContent = { message ->
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
                                Box {
                                    Column(
                                        modifier = Modifier
                                            .zIndex(0f)
                                    ) {
                                        BookShelf(
                                            bookDataViewModel = bookDataViewModel,
                                            recentBooks = recentBooks,
                                            favouritesBooks = favouritesBooks,
                                            toReadBooks = toReadBooks,
                                            particularCollection = particularCollection,
                                            completedBooks = completedBooks,
                                            listOfCollections = listOfCollections,
                                            currentBookShelf = currentBookShelf,
                                        )
                                    }
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
                                                    when (snackBarData.visuals.message) {
                                                        "Added to \"Favourites\" " -> selectedBook?.let {
                                                            bookDataViewModel.toggleFavorite(
                                                                it
                                                            )
                                                        }

                                                        "Added to \"To Read\" " -> selectedBook?.let {
                                                            bookDataViewModel.toggleToRead(
                                                                it
                                                            )
                                                        }

                                                        "Added to \"Done Reading\" " -> selectedBook?.let {
                                                            bookDataViewModel.toggleDoneReading(
                                                                it
                                                            )
                                                        }

                                                        "Removed from \"Favourites\" " -> selectedBook?.let {
                                                            bookDataViewModel.toggleFavorite(
                                                                it
                                                            )
                                                        }

                                                        "Removed from \"To Read\" " -> selectedBook?.let {
                                                            bookDataViewModel.toggleToRead(
                                                                it
                                                            )
                                                        }

                                                        "Removed from \"Done Reading\" " -> selectedBook?.let {
                                                            bookDataViewModel.toggleDoneReading(
                                                                it
                                                            )
                                                        }
                                                    }
                                                },
                                                modifier = modifier.padding(bottom = 6.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    BottomBar(
                        navController = navController,
                        bookDataViewModel = bookDataViewModel,
                        timeGoalViewModel = timeGoalViewModel,
                        toCloseDrawer = toCloseDrawer,
                        selectedBook = selectedBook,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            } else {
                AboutDoucument(
                    bookDataViewModel = bookDataViewModel,
                    timeGoalViewModel = timeGoalViewModel,
                    navController = navController,
                    showAboutDocument = showAboutDocument,
                    onAboutDocumentClicked = {showAboutDocument = false},
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    modifier = modifier
                )
            }
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
fun GernealDrawerTopBar(
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
            IconButton(
                onClick = { toOpenDrawer() }
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
            modifier = Modifier
                .align(Alignment.TopEnd),
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
    bookDataViewModel: BookDataViewModel,
    timeGoalViewModel: TimeGoalViewModel,
    navController: NavController,
    toCloseDrawer: () -> Unit,
    selectedBook: Book?,
    modifier: Modifier = Modifier
) {
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
                IconButton(
                    onClick = {
                        navController.navigate("StatsScreen")
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chart),
                        contentDescription = "Stats",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = {
                        selectedBook?.let {
                            bookDataViewModel.updateBookTime(it)

                            navController.navigate("BookScreen/${encodedUri}")
                        }
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
                timeGoalViewModel = timeGoalViewModel,
                navController = navController,
                toCloseDrawer = toCloseDrawer,
            )
        }
    }
}

