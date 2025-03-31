package com.example.epubreader

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.epubreader.model.BookDatabase
import com.example.epubreader.model.BookRepository
import com.example.epubreader.ui.theme.EPUBReaderTheme

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

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        setContent {
            EPUBReaderTheme {
                App()
            }
        }
    }
}

@Composable
fun App(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val application = context.applicationContext as Application

    val database = BookDatabase.getDatabase(context)
    val bookDao = database.BookDao()

    val repository = BookRepository(bookDao)

    val factory = BookViewModelFactory(application, repository)

    val bookDataViewModel: BookDataViewModel = viewModel(factory = factory)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: "homeScreen"

    ModalNavigationDrawer(
        gesturesEnabled = drawerState.isOpen,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(
                    topEnd = dimensionResource(id = R.dimen.padding_small),
                    bottomEnd = dimensionResource(id = R.dimen.padding_small)
                ),
                drawerContainerColor = MaterialTheme.colorScheme.onBackground,
                drawerContentColor = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.width(290.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(state = rememberScrollState())
                ) {
                    Drawer(
                        navController = navController,
                        bookDataViewModel = bookDataViewModel,
                        drawerState = drawerState,
                        currentScreen = currentScreen
                    )
                }
            }
        },
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

                composable(
                    route = "homeScreen",
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, exitTransitionSpec)
                    },
                    popEnterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, enterTransitionSpec)
                    }
                ) {
                    HomeScreen(
                        bookDataViewModel = bookDataViewModel,
                        navController = navController,
                        drawerState = drawerState,
                        modifier = Modifier.padding(innerPadding),
                        currentScreen = currentScreen
                    )
                }
                composable(
                    route = "aboutDocument",
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, enterTransitionSpec)
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, exitTransitionSpec)
                    },
                    popEnterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, enterTransitionSpec)
                    },
                    popExitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, exitTransitionSpec)
                    }

                ){
                    AboutDoucument(
                        bookDataViewModel = bookDataViewModel,
                        navController = navController,
                        currentScreen = currentScreen,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                composable(
                    route = "EditScreen",
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, enterTransitionSpec)
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, exitTransitionSpec)
                    },
                    popExitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, exitTransitionSpec)
                    }

                ){
                    EditScreen(
                        navController = navController,
                        bookDataViewModel = bookDataViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }


                composable(
                    route = "BookScreen/{pdfUri}",
                    arguments = listOf(navArgument("pdfUri") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, enterTransitionSpec)
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, exitTransitionSpec)
                    },
                ){ backStackEntry ->

                    val pdfUri = backStackEntry.arguments?.getString("pdfUri")
                    PDFViewerScreen(
                        bookDataViewModel = bookDataViewModel,
                        navController,
                        pdfUri
                    )
                    }
            }
        }
    }
}
