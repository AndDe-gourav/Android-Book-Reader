package com.example.bookReader

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookReader.ui.theme.BookStateViewModel
import com.example.bookReader.ui.theme.CollectionViewModel
import com.example.bookReader.ui.theme.DrawerContent
import com.example.bookReader.ui.theme.EPUBReaderTheme
import com.example.bookReader.ui.theme.EditScreen
import com.example.bookReader.ui.theme.HomeScreenImproved
import com.example.bookReader.ui.theme.LibraryViewModel
import com.example.bookReader.ui.theme.PdfReaderScreen
import com.example.bookReader.ui.theme.PdfViewerViewModel
import com.example.bookReader.ui.theme.StatsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        setContent {
            EPUBReaderTheme {
                App()
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun App(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: "homeScreen"

    // Get ViewModels using Hilt
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val bookStateViewModel: BookStateViewModel = hiltViewModel()
    val collectionViewModel: CollectionViewModel = hiltViewModel()
    val pdfViewerViewModel: PdfViewerViewModel = hiltViewModel()

    var showTimeGoal by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContentColor = MaterialTheme.colorScheme.background,
                drawerContainerColor = MaterialTheme.colorScheme.onBackground
            ) {
                DrawerContent(
                    libraryViewModel = libraryViewModel,
                    bookStateViewModel = bookStateViewModel,
                    collectionViewModel = collectionViewModel,
                    navController = navController,
                    onBackPressed = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    toCloseDrawer = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    currentScreen = currentScreen,
                    modifier = Modifier
                        .width(290.dp)
                        .verticalScroll(
                            state = rememberScrollState()
                        )
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "homeScreen"
            ) {
                val enterTransitionSpec = tween<IntOffset>(250)
                val exitTransitionSpec = tween<IntOffset>(250)

                // Home Screen
                composable(
                    route = "homeScreen",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            enterTransitionSpec
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            exitTransitionSpec
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            enterTransitionSpec
                        )
                    }
                ) {
                    HomeScreenImproved(
                        navController = navController,
                        toCloseDrawer = { scope.launch { drawerState.close() } },
                        toOpenDrawer = { scope.launch { drawerState.open() } },
                        libraryViewModel = libraryViewModel,
                        bookStateViewModel = bookStateViewModel,
                        collectionViewModel = collectionViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                // Stats Screen
                composable(
                    route = "StatsScreen",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            enterTransitionSpec
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            exitTransitionSpec
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            exitTransitionSpec
                        )
                    }
                ) {
                    StatsScreen(
                        navController = navController,
                        libraryViewModel = libraryViewModel,
                        bookStateViewModel = bookStateViewModel,
                        pdfViewerViewModel = pdfViewerViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                // Edit Screen
                composable(
                    route = "EditScreen",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            enterTransitionSpec
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            exitTransitionSpec
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            exitTransitionSpec
                        )
                    }
                ) {
                    EditScreen(
                        navController = navController,
                        libraryViewModel = libraryViewModel,
                        bookStateViewModel = bookStateViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                // PDF Reader Screen - Updated to use bookId instead of URI
                composable(
                    route = "pdfReader/{bookId}",
                    arguments = listOf(navArgument("bookId") { type = NavType.LongType }),
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            enterTransitionSpec
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            exitTransitionSpec
                        )
                    },
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable

                    PdfReaderScreen(
                        bookId = bookId,
                        navController = navController,
                        libraryViewModel = libraryViewModel,
                        pdfViewerViewModel = pdfViewerViewModel,
                        bookStateViewModel = bookStateViewModel,
                        showTimeGoal = showTimeGoal,
                        onTimeGoalClicked = { showTimeGoal = !showTimeGoal }
                    )
                }
            }
        }
    }
}