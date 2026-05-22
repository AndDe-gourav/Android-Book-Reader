package com.timepass.bookreader.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.timepass.bookreader.R
import com.timepass.bookreader.UiEventManager
import com.timepass.bookreader.navigation.AboutBookScreen
import com.timepass.bookreader.navigation.EditScreen
import com.timepass.bookreader.navigation.HomeScreen
import com.timepass.bookreader.navigation.PdfReader
import com.timepass.bookreader.navigation.StatsScreen
import com.timepass.bookreader.theme.BookReaderTheme
import com.timepass.bookreader.ui.aboutbook.AboutBookScreen
import com.timepass.bookreader.ui.aboutbook.EditScreen
import com.timepass.bookreader.ui.home.BookStateViewModel
import com.timepass.bookreader.ui.home.BottomBar
import com.timepass.bookreader.ui.home.CollectionViewModel
import com.timepass.bookreader.ui.home.GeneralDrawerTopBar
import com.timepass.bookreader.ui.home.HomeScreen
import com.timepass.bookreader.ui.home.LibraryViewModel
import com.timepass.bookreader.ui.pdfviewer.PdfReaderScreen
import com.timepass.bookreader.ui.pdfviewer.PdfViewerViewModel
import com.timepass.bookreader.ui.stats.StatsScreen
import com.timepass.bookreader.ui.stats.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        setContent {
            BookReaderTheme {
                App()
            }
        }
    }
}


@Composable
fun App(
    modifier: Modifier = Modifier,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    bookStateViewModel: BookStateViewModel = hiltViewModel(),
    collectionViewModel: CollectionViewModel = hiltViewModel(),
    pdfViewerViewModel: PdfViewerViewModel = hiltViewModel(),
    statsViewModel: StatsViewModel = hiltViewModel(),
) {

    val backStack = rememberNavBackStack(HomeScreen)
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedBook by libraryViewModel.selectedBook.collectAsState()

    LaunchedEffect(Unit) {
        UiEventManager.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (backStack.lastOrNull() == HomeScreen) {
                GeneralDrawerTopBar(
                    text = "Home",
                    libraryViewModel = libraryViewModel,
                    modifier = Modifier.zIndex(1f)
                )
            } else if (backStack.lastOrNull() is PdfReader){

            } else {
                TopBar(
                    onActionClicked = { backStack.removeLastOrNull() },
                    titleText =
                        when (backStack.lastOrNull()) {
                            StatsScreen -> "Stats"
                            EditScreen -> "Edit Book"
                            AboutBookScreen -> "About Book"
                            else -> { "Not Available" }
                        },
                    icon = R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            ){ snackbarData ->
                Snackbar(
                    shape = RoundedCornerShape(0.dp),
                    containerColor = MaterialTheme.colorScheme.outline,
                    contentColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(10.dp)
                ){
                    Text(
                        text = snackbarData.visuals.message
                    )
                }
            }
        },
        bottomBar = {
            if (backStack.lastOrNull() == HomeScreen)
            BottomBar(
                openPdf = { bookId -> backStack.add(PdfReader(bookId)) },
                openStats = { backStack.add(StatsScreen) },
                libraryViewModel = libraryViewModel,
                selectedBook = selectedBook,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            transitionSpec = {
                slideInHorizontally( initialOffsetX = { it }, animationSpec = tween(180)
                ) + fadeIn( animationSpec = tween(120)
                ) togetherWith
                        slideOutHorizontally( targetOffsetX = { -it / 4 }, animationSpec = tween(180)
                        ) + fadeOut( animationSpec = tween(120)
                )
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 }, animationSpec = tween(180)
                ) + fadeIn( animationSpec = tween(120)
                ) togetherWith
                        slideOutHorizontally( targetOffsetX = { it }, animationSpec = tween(180)
                        ) + fadeOut( animationSpec = tween(120)
                )
            },
            entryProvider = entryProvider {
                entry<HomeScreen> {
                    HomeScreen(
                        openPdf = { bookId -> backStack.add(PdfReader(bookId)) },
                        openStats = { backStack.add(StatsScreen) },
                        openEdit = { backStack.add(EditScreen) },
                        openAbout = { backStack.add(AboutBookScreen) },
                        libraryViewModel = libraryViewModel,
                        bookStateViewModel = bookStateViewModel,
                        collectionViewModel = collectionViewModel,
                        modifier = modifier.padding(innerPadding)
                    )
                }

                entry<StatsScreen> {
                    StatsScreen(
                        onBack = { backStack.removeLastOrNull() },
                        statsViewModel = statsViewModel,
                        modifier = modifier.padding(innerPadding)
                    )
                }

                entry<EditScreen> {
                    EditScreen(
                        onBack = { backStack.removeLastOrNull() },
                        libraryViewModel = libraryViewModel,
                        modifier = modifier.padding(innerPadding)
                    )
                }

                entry<AboutBookScreen> {
                    AboutBookScreen(
                        onBack = { backStack.removeLastOrNull() },
                        openPdf = { bookId -> backStack.add(PdfReader(bookId)) },
                        openEdit = { backStack.add(EditScreen) },
                        libraryViewModel = libraryViewModel,
                        bookStateViewModel = bookStateViewModel,
                        collectionViewModel = collectionViewModel,
                        modifier = modifier.padding(innerPadding)
                    )
                }

                entry<PdfReader> { pdfReader ->
                    PdfReaderScreen(
                        onBack = { backStack.removeLastOrNull() },
                        bookId = pdfReader.bookId,
                        libraryViewModel = libraryViewModel,
                        bookStateViewModel = bookStateViewModel,
                        pdfViewerViewModel = pdfViewerViewModel,
                        modifier = modifier
                    )
                }
            }
        )
    }
}