package com.example.bookReader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun getActivity(): Activity? {
    var context = LocalContext.current
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewerScreen(
    pdfUri: String?,
    navController: NavController,
    showTimeGoal: Boolean,
    onTimeGoalClicked: () -> Unit,
) {
    var timeGoal: Int? by rememberSaveable { mutableStateOf(0) }
    var sessionStartTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var sessionTimeSpent by rememberSaveable { mutableLongStateOf( 0L) }
    var timeGoalAvailable by rememberSaveable { mutableStateOf(false) }
    var totalTime: Long? by rememberSaveable { mutableStateOf(0L) }
    var isTimerEnabled by rememberSaveable { mutableStateOf(false) }


    val uri = pdfUri?.toUri()!!

    val activity = getActivity()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    var isColorPaletteVisible by remember { mutableStateOf(false) }
    var isTimerVisible by remember { mutableStateOf(false) }
    var lockHorizontalMovement by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current


    var isTocSheetVisible by remember { mutableStateOf(false) }

    var lastOpenedPage by rememberSaveable { mutableIntStateOf(0) }

    var isSystemUIVisible by remember { mutableStateOf(true) }


    BackHandler {
        if (isTocSheetVisible){
            isTocSheetVisible = false
        }else{
            navController.navigate("homeScreen")
        }
    }

    if (timeGoalAvailable) {
        DisposableEffect(lifecycleOwner) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        sessionStartTime = System.currentTimeMillis()
                        sessionTimeSpent = 0
                        scope.launch {
                        }
                    }

                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        }
    }
        DisposableEffect(Unit) {
            onDispose {
                pdfUri.let {
                    CoroutineScope(Dispatchers.IO).launch {
                    }
                }
                lockHorizontalMovement = false
            }
        }
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Image(
                painter = painterResource(id = R.drawable.backgroudcamel),
                contentDescription = "Camel",
                modifier = Modifier.align(Alignment.Center)
            )

            PdfTopBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeContent)
                    .align(Alignment.TopCenter)
                    .systemBarsPadding(),
                onBackClicked = { navController.navigate("homeScreen") },
                isTocSheetVisible = isTocSheetVisible,
                onTocClicked = { isTocSheetVisible = !isTocSheetVisible },
                navController = navController,
                isSystemUIVisible = isSystemUIVisible
            )
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeContent)
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding( bottom = 4.dp)
                ) {

                    Spacer(
                        modifier = Modifier.size(20.dp)
                    )
                    AnimatedVisibility(
                        visible = isSystemUIVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
                    ) {
                       PdfBottomBar(
                           isColorPaletteVisible = isColorPaletteVisible,
                           isTimerVisible = isTimerVisible,
                           isHorizontalLocked = lockHorizontalMovement,
                           onThemeClicked = {
                               isTimerVisible = false
                               isColorPaletteVisible = !isColorPaletteVisible
                           },
                           onLockClicked = {
                               isTimerVisible = false
                               isColorPaletteVisible = false
                               if (!lockHorizontalMovement) {
                                   Toast.makeText(
                                       context,
                                       "Horizontal Movement Locked",
                                       Toast.LENGTH_SHORT
                                   ).show()
                               }
                               lockHorizontalMovement = !lockHorizontalMovement
                           },
                           onTimerClicked = {
                               if (timeGoalAvailable) {
                                   isTimerVisible = !isTimerVisible
                               }
                               isColorPaletteVisible = false
                               if (!timeGoalAvailable){
                                   onTimeGoalClicked()
                               }
                           },
                           isTimerEnabled = isTimerEnabled,
                       )
                    }
                }
            if(showTimeGoal) {

            }
            AnimatedVisibility (
                visible = isTocSheetVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = 0.3f))
                        .fillMaxSize()
                        .clickable(
                            onClick = {
                                isTocSheetVisible = !isTocSheetVisible
                            }
                        )
                )
            }
                AnimatedVisibility(
                    visible = isTocSheetVisible,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight }
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight }
                    )
                ) {

                }


        }

}

fun openPdf(context: Context, uri: Uri) {
    val intent = Intent(
        context,
        com.artifex.mupdf.viewer.DocumentActivity::class.java
    ).apply {
        action = Intent.ACTION_VIEW
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }

    context.startActivity(intent)
}





@Composable
fun PickAndOpenPdf() {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { openPdf(context, it) }
    }

    Button(onClick = {
        launcher.launch(arrayOf("application/pdf"))
    }) {
        Text("Open PDF")
    }
}

