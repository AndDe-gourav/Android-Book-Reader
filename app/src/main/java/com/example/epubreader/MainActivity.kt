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
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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


@OptIn(ExperimentalFoundationApi::class)
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

    var drawerState = remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: "homeScreen"

    val density = LocalDensity.current
    val drawerWidthPx  =  with(density) { 250.dp.toPx() }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val anchors = DraggableAnchors {
            DrawerValue.Open at drawerWidthPx
            DrawerValue.Closed at 0f
        }

        val draggableState = remember {
            AnchoredDraggableState(
                initialValue = DrawerValue.Closed,
                anchors = anchors,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { with(density) { 80.dp.toPx() } },
                snapAnimationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                decayAnimationSpec = exponentialDecay( )
            )
        }

        LaunchedEffect(drawerState.value) {
            val target = if (drawerState.value) DrawerValue.Open else DrawerValue.Closed
            draggableState.animateTo(
                targetValue = target,
            )
        }

        LaunchedEffect(draggableState.currentValue) {
            drawerState.value = draggableState.currentValue == DrawerValue.Open
        }


        Drawer(
            bookDataViewModel = bookDataViewModel,
            navController = navController,
            toCloseDrawer = { drawerState.value = false },
            currentScreen = currentScreen,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier
                .graphicsLayer {
                    this.translationX = draggableState.requireOffset()
                    val scale = androidx.compose.ui.util.lerp(
                        1f,
                        0.8f,
                        draggableState.requireOffset() / drawerWidthPx
                    )
                    this.scaleX = scale
                    this.scaleY = scale
                    this.shape = RoundedCornerShape(if (drawerState.value) 16.dp else 0.dp)
                    this.clip = true
                }
                .anchoredDraggable(draggableState, Orientation.Horizontal),
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
                    HomeScreen(
                        bookDataViewModel = bookDataViewModel,
                        navController = navController,
                        drawerState = drawerState.value,
                        toCloseDrawer = { drawerState.value = false },
                        toOpenDrawer = { drawerState.value = true },
                        modifier = Modifier
                            .padding(innerPadding)
                            .clickable(onClick ={ if (draggableState.currentValue == DrawerValue.Open) drawerState.value = false})

                    )
                }
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
                        bookDataViewModel = bookDataViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }


                composable(
                    route = "BookScreen/{pdfUri}",
                    arguments = listOf(navArgument("pdfUri") { type = NavType.StringType }),
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
