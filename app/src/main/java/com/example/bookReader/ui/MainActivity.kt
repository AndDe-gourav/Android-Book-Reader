package com.example.bookReader.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookReader.theme.BookReaderTheme
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
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                        )
                    }
                ) {innerPadding ->
                    App(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState,
                    )
                }
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
    snackbarHostState: SnackbarHostState,
) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        libraryViewModel.uiEvent.collect { event ->
            if (event is UiEvent.ShowSnackbar) snackbarHostState.showSnackbar(event.message)
        }
    }

    LaunchedEffect(Unit) {
        bookStateViewModel.uiEvent.collect { event ->
            if (event is UiEvent.ShowSnackbar) snackbarHostState.showSnackbar(event.message)
        }
    }

    LaunchedEffect(Unit) {
        collectionViewModel.uiEvent.collect { event ->
            if (event is UiEvent.ShowSnackbar) snackbarHostState.showSnackbar(event.message)
        }
    }

    LaunchedEffect(Unit) {
        pdfViewerViewModel.uiEvent.collect { event ->
            if (event is UiEvent.ShowSnackbar) snackbarHostState.showSnackbar(event.message)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "homeScreen",
        enterTransition = { slideInHorizontally( animationSpec = tween(300),
            initialOffsetX = { fullWidth -> fullWidth  } )+ fadeIn(tween(200)) },
        exitTransition = { slideOutHorizontally( animationSpec = tween(300),
            targetOffsetX = { fullWidth -> -fullWidth } )+ fadeOut(tween(200)) },
        popEnterTransition = { slideInHorizontally( animationSpec = tween(300),
            initialOffsetX = { fullWidth -> -fullWidth  } ) + fadeIn(tween(200)) },
        popExitTransition = { slideOutHorizontally( animationSpec = tween(300),
            targetOffsetX = { fullWidth -> fullWidth } ) + fadeOut(tween(200)) },
    ) {

        composable(
            route = "homeScreen",
        ) {
            HomeScreen(
                navController = navController,
                libraryViewModel = libraryViewModel,
                bookStateViewModel = bookStateViewModel,
                collectionViewModel = collectionViewModel,
                modifier = modifier
            )
        }

        composable(
            route = "StatsScreen",
        ) {
            StatsScreen(
                navController = navController,
                statsViewModel = statsViewModel,
                modifier = modifier
            )
        }

        composable(
            route = "EditScreen",
        ) {
            EditScreen(
                navController = navController,
                libraryViewModel = libraryViewModel,
                modifier = modifier
            )
        }

        composable(
            route = "AboutBookScreen",
        ) {
            AboutBookScreen(
                navController = navController,
                libraryViewModel = libraryViewModel,
                bookStateViewModel = bookStateViewModel,
                collectionViewModel = collectionViewModel,
                modifier = modifier
            )
        }

        composable(
            route = "pdfReader/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable

            PdfReaderScreen(
                bookId = bookId,
                navController = navController,
                libraryViewModel = libraryViewModel,
                bookStateViewModel = bookStateViewModel,
                pdfViewerViewModel = pdfViewerViewModel,
                modifier = modifier
            )
        }
    }
}