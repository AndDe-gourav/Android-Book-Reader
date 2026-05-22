package com.timepass.bookreader.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
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
import com.timepass.bookreader.ui.home.CollectionViewModel
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

    NavDisplay(
        backStack = backStack,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it }
            ) + fadeIn() togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 }
                    ) + fadeOut()
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 }
            ) + fadeIn() togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it }
                    ) + fadeOut()
        },
        entryProvider = entryProvider {
            entry<HomeScreen> {
                HomeScreen(
                    snackbarHostState = snackbarHostState,
                    openPdf = { bookId -> backStack.add(PdfReader(bookId)) },
                    openStats = { backStack.add(StatsScreen) },
                    openEdit = { backStack.add(EditScreen) },
                    openAbout = { backStack.add(AboutBookScreen) },
                    libraryViewModel = libraryViewModel,
                    bookStateViewModel = bookStateViewModel,
                    collectionViewModel = collectionViewModel,
                    modifier = modifier
                )
            }

            entry<StatsScreen> {
                StatsScreen(
                    snackbarHostState = snackbarHostState,
                    onBack = { backStack.removeLastOrNull() },
                    statsViewModel = statsViewModel,
                    modifier = modifier
                )
            }

            entry<EditScreen> {
                EditScreen(
                    snackbarHostState = snackbarHostState,
                    onBack = { backStack.removeLastOrNull() },
                    libraryViewModel = libraryViewModel,
                    modifier = modifier
                )
            }

            entry<AboutBookScreen> {
                AboutBookScreen(
                    snackbarHostState = snackbarHostState,
                    onBack = { backStack.removeLastOrNull() },
                    openPdf = { bookId -> backStack.add(PdfReader(bookId)) },
                    openEdit = { backStack.add(EditScreen) },
                    libraryViewModel = libraryViewModel,
                    bookStateViewModel = bookStateViewModel,
                    collectionViewModel = collectionViewModel,
                    modifier = modifier
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