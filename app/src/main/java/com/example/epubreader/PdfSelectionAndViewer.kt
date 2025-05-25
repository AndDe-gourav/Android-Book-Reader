package com.example.epubreader

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    val uri = pdfUri?.toUri()

    val activity = getActivity()

    val scope = rememberCoroutineScope()

    val lastOpenedPageDB by bookDataViewModel.lastPage.collectAsState()
    var lastOpenedPage by remember { mutableIntStateOf(0) }

    var isSystemUIVisible by remember { mutableStateOf(true) }

    BackHandler {
        showSystemBars(activity!!)
        navController.navigate("homeScreen")
    }


    LaunchedEffect(Unit) {
        if (pdfUri != null) {
            bookDataViewModel.fetchLastPage(pdfUri)
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            pdfUri?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    bookDataViewModel.updateLastPage(pdfUri, lastOpenedPage)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { ctx -> PDFView(ctx, null).apply {
                setBackgroundColor(0xFFFFF1E5.toInt())
                }
            },
            update = { pdfView ->
                pdfView.fromUri(uri)
                    .pageFitPolicy(FitPolicy.BOTH)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(lastOpenedPageDB)
                    .enableAnnotationRendering(false)
                    .scrollHandle(DefaultScrollHandle(pdfView.context))
                    .spacing(20)
                    .enableAntialiasing(true)
                    .nightMode(false)
                    .pageSnap(false)
                    .onPageChange { page, _ ->
                        lastOpenedPage = page
                    }
                    .onTap {
                        if (!isSystemUIVisible) {
                            showSystemBars(activity!!)
                            isSystemUIVisible = true
                        }else {
                            hideSystemBars(activity!!)
                            isSystemUIVisible = false
                        }
                        true
                    }
                    .scrollHandle(
                        DefaultScrollHandle(pdfView.context)
                    )
                    .onLoad {
                        pdfView.useBestQuality(true)
                        scope.launch {
                            val meta = pdfView.documentMeta
                            val toc = pdfView.tableOfContents
                            for (bookmark in toc) {
                                Log.d("TOC", "Title: ${bookmark.title}, Page: ${bookmark.pageIdx}")
                                if (bookmark.children.isNotEmpty()) {
                                    for (child in bookmark.children) {
                                        Log.d("TOC", "Has children: Title: ${child.title}, Page: ${child.pageIdx}")
                                    }
                                }
                            }
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


            },
            modifier = Modifier.fillMaxSize()
        )

    }

}
