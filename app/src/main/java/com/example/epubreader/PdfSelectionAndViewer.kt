package com.example.epubreader

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun PDFSelection(
    navController: NavController,
    bookDataViewModel: BookDataViewModel,
    toCloseDrawer: () -> Unit,
) {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {

            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(it, takeFlags)

            val encodedUri = Uri.encode(it.toString())
            bookDataViewModel.addBook(it)
            bookDataViewModel.fetchLastPage(it.toString())
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
                    toCloseDrawer()
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.load_circle_fill),
                contentDescription = "Home",
                tint = Color.Black
            )
            Spacer(
                Modifier.width(dimensionResource(id = R.dimen.padding_large))
            )
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }
    }
}

@Composable
fun QuickPdfSelection(
    navController: NavController,
    bookDataViewModel: BookDataViewModel,
    toCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver


    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {

            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(it, takeFlags)

            val encodedUri = Uri.encode(it.toString())
            bookDataViewModel.addBook(it)
            bookDataViewModel.fetchLastPage(it.toString())
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
                            toCloseDrawer()
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
    bookDataViewModel: BookDataViewModel,
    pdfUri: String?,
    navController: NavController,
    showTimeGoal: Boolean,
    onTimeGoalClicked: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (pdfUri != null) {
            bookDataViewModel.fetchLastPage(pdfUri)
            Log.d("page", "${bookDataViewModel.lastPage.value}")
        }
    }


    val uri = pdfUri?.toUri()

    val activity = getActivity()

    val scope = rememberCoroutineScope()

    var totalPages by remember { mutableIntStateOf(0) }

    var colorTheme: PDFView.Theme? by remember { mutableStateOf(PDFView.Theme.LIGHT) }
    var isColorPaletteVisible by remember { mutableStateOf(false) }
    var lockHorizontalMovement by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current


    var isTocSheetVisible by remember { mutableStateOf(false) }

    val lastOpenedPageDB by bookDataViewModel.lastPage.collectAsState()
    var lastOpenedPage by remember { mutableIntStateOf(0) }
    var jumpToPage by remember { mutableIntStateOf(lastOpenedPageDB) }

    var isSystemUIVisible by remember { mutableStateOf(true) }

    val contentResolver = LocalContext.current.contentResolver

    val inputStream = contentResolver.openInputStream(uri!!)

    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.0-flash")

//    LaunchedEffect(Unit) {
//        inputStream?.use { stream ->
//            val prompt = content {
//                inlineData(
//                    bytes = stream.readBytes(),
//                    mimeType = "application/pdf"
//                )
//                text("can you tell me about the book")
//            }
//
//            val response = model.generateContent(prompt)
//            Log.d("Result", "${response.text}")
//        }
//    }
    BackHandler {
        if (isTocSheetVisible){
            isTocSheetVisible = false
        }else{
            navController.navigate("homeScreen")
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            pdfUri?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    bookDataViewModel.updateLastPage(pdfUri, lastOpenedPage)
                }
            }
            showSystemBars(activity!!)
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

            AndroidView(
                factory = { ctx ->
                    PDFView(ctx, null).apply {
                        setBackgroundColor(Color.Transparent.hashCode())
                            fromUri(uri)
                            .useBestQuality(true)
                            .pageFitPolicy(FitPolicy.BOTH)
                            .enableDoubletap(!lockHorizontalMovement)
                            .enableSwipe(true)
                            .scrollHandle(DefaultScrollHandle(ctx))
                            .defaultPage(lastOpenedPageDB)
                            .spacing(1)
                            .onPageChange { page, _ ->
                                lastOpenedPage = page
                                jumpToPage = page
                            }
                            .onTap {
                                scope.launch {
                                    if (isColorPaletteVisible) {
                                        isColorPaletteVisible = false
                                        if (isSystemUIVisible) {
                                            delay(1000)
                                        }
                                    }
                                    if (!isSystemUIVisible) {
                                        showSystemBars(activity!!)
                                        isSystemUIVisible = true
                                    } else {
                                        hideSystemBars(activity!!)
                                        isSystemUIVisible = false
                                    }
                                    }
                                true
                            }
                            .onLoad {
                                scope.launch {
                                    Log.d("start ", "$lastOpenedPageDB")
                                    val meta = this@apply.documentMeta
                                    totalPages = this@apply.pageCount
                                    bookDataViewModel.updateToc( this@apply.tableOfContents )
                                    if (meta != null) {
                                        val title = meta.title ?: ""
                                        val author = meta.author ?: ""
                                        val bookFromUri = bookDataViewModel.getBookFromUri(pdfUri!!)

                                        if (bookFromUri?.title == "Untitled" && title.isNotBlank()) {
                                            bookDataViewModel.updateBookTitle(bookFromUri, title)
                                        }

                                        if (bookFromUri?.author == "Unknown Author" && author.isNotBlank()) {
                                            bookDataViewModel.updateBookAuthor(bookFromUri, author)
                                        }
                                    }

                                }
                            }
                            .load()

                    }
                },
                update = { pdfView ->
                    if (jumpToPage != lastOpenedPage) {
                        pdfView.jumpTo(jumpToPage, true)
                        lastOpenedPage = jumpToPage
                    }
                    pdfView.setTheme(colorTheme)
                    pdfView.lockHorizontalMovement(lockHorizontalMovement)
                    pdfView.enableDoubletap(!lockHorizontalMovement)
                    pdfView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isSystemUIVisible) {
                PdfTopBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeContent)
                        .align(Alignment.TopCenter)
                        .systemBarsPadding(),
                    onBackClicked = { navController.navigate("homeScreen")},
                    onOptionsClicked = { },
                    isTocSheetVisible = isTocSheetVisible,
                    onTocClicked = { isTocSheetVisible = !isTocSheetVisible }
                )
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeContent)
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding( bottom = 4.dp)
                ) {
                    AnimatedVisibility(
                        visible = isColorPaletteVisible,
                    ) {
                        val colorToThemeMap = mapOf(
                            Color.Black to PDFView.Theme.NIGHT,
                            Color.White to PDFView.Theme.LIGHT,
                            colorResource(id = R.color.Sepia) to PDFView.Theme.SEPIA,
                            colorResource(id = R.color.Dark_Sepia) to PDFView.Theme.DARK_SEPIA
                        )
                        ThemeSelector(
                            colorsList = listOf(
                                Color.White,
                                colorResource(id = R.color.Sepia),
                                colorResource(id = R.color.Dark_Sepia),
                                Color.Black
                            ),
                            onColorChange = { color ->
                                colorTheme = colorToThemeMap[color]
                            },
                        )
                    }
                    Spacer(
                        modifier = Modifier.size(20.dp)
                    )
                    AnimatedVisibility(
                        visible = isSystemUIVisible,
                        enter = expandHorizontally(
                            animationSpec = spring(

                            ),
                            expandFrom = Alignment.Start
                        ),
                        exit = shrinkHorizontally(
                            shrinkTowards = Alignment.Start
                        )
                    ) {
                       PdfBottomBar(
                           isColorPaletteVisible = isColorPaletteVisible,
                           isHorizontalLocked = lockHorizontalMovement,
                           onThemeClicked = {
                               isColorPaletteVisible = !isColorPaletteVisible
                           },
                           onLockClicked = {
                               lockHorizontalMovement = !lockHorizontalMovement
                           },
                           onTimerClicked = {
                               onTimeGoalClicked()
                           }
                       )
                    }
                }
            }
            if(showTimeGoal)
            TimePicker(
                onDismissRequest = { onTimeGoalClicked() },
                bookDataViewModel = bookDataViewModel
            )
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
                    TocSheet(
                        currentPage = lastOpenedPage,
                        bookDataViewModel = bookDataViewModel,
                        onTocPageClicked = { tocPage ->
                            jumpToPage = tocPage
                            isTocSheetVisible = !isTocSheetVisible
                        },
                        onChildPageClicked = { childPage ->
                            jumpToPage = childPage
                            isTocSheetVisible = !isTocSheetVisible
                        },
                        modifier = Modifier.windowInsetsPadding(if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) WindowInsets.safeContent else WindowInsets(0,0,0,0))
                    )
                }


        }

}

