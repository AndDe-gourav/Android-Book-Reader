package com.example.epubreader

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
    navController: NavController,
    pdfUri: String?
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

    val lastOpenedPageDB by bookDataViewModel.lastPage.collectAsState()
    var lastOpenedPage by remember { mutableIntStateOf(0) }

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


    DisposableEffect(Unit) {
        onDispose {
            pdfUri?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    bookDataViewModel.updateLastPage(pdfUri, lastOpenedPage)
                }
            }
            showSystemBars(activity!!)
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
//                                val toc = pdfView.tableOfContents
//                                for (bookmark in toc) {
//                                    Log.d(
//                                        "TOC",
//                                        "Title: ${bookmark.title}, Page: ${bookmark.pageIdx}"
//                                    )
//                                    if (bookmark.children.isNotEmpty()) {
//                                        for (child in bookmark.children) {
//                                            Log.d("TOC", "Has children: Title: ${child.title}, Page: ${child.pageIdx}")
//                                        }
//                                    }
//                                }
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
                    pdfView.setTheme(colorTheme)
                    pdfView.lockHorizontalMovement(lockHorizontalMovement)
                    pdfView.enableDoubletap(!lockHorizontalMovement)
                    pdfView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
            val rotationAnimation by animateFloatAsState(
                targetValue = if (!isColorPaletteVisible)0f else -45f
            )

            if (isSystemUIVisible) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeContent)
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding(start = 20.dp, bottom = 4.dp)
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
                        visible = isSystemUIVisible
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    onClick = { isColorPaletteVisible = !isColorPaletteVisible },
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .size(50.dp)
                                        .rotate(rotationAnimation)
                                        .border(
                                            if (isColorPaletteVisible) 2.dp else 0.dp,
                                            MaterialTheme.colorScheme.inverseSurface,
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.paint_roller),
                                        contentDescription = "ColorPicker",
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { lockHorizontalMovement = !lockHorizontalMovement },
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .size(50.dp)
                                    .border(
                                        if (lockHorizontalMovement) 2.dp else 0.dp,
                                        MaterialTheme.colorScheme.inverseSurface,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.solar_lock_broken),
                                    contentDescription = "LockHorizontalMovement",
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                    }
                }
            }


//            Surface(
//                color = MaterialTheme.colorScheme.onBackground,
//                shape = RoundedCornerShape(8.dp),
//                modifier = Modifier
//                    .align(Alignment.TopCenter)
//                    .systemBarsPadding()
//                    .padding(top = 50.dp)
//            ) {
//                Text(
//                    text = " $lastOpenedPage / $totalPages",
//                    modifier = Modifier.padding(15.dp),
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//            }

        }

}

