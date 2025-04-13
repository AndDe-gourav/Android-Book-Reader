package com.example.epubreader

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
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
            )
            Spacer(
                Modifier.width(dimensionResource(id = R.dimen.padding_large))
            )
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.bodyLarge,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewerScreen(
    bookDataViewModel: BookDataViewModel,
    navController: NavController,
    pdfUri: String?
) {
    val context = LocalContext.current
    val uri = pdfUri?.toUri()

    val lastOpenedPageDB by bookDataViewModel.lastPage.collectAsState()
    var lastOpenedPageL by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (pdfUri != null) {
            bookDataViewModel.fetchLastPage(pdfUri)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            pdfUri?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    bookDataViewModel.updateLastPage(pdfUri, lastOpenedPageL)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("PDF Viewer") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            modifier = Modifier.zIndex(1f)
        )
        AndroidView(
            factory = { ctx -> PDFView(ctx, null) },
            update = { pdfView ->
                pdfView.fromUri(uri)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(lastOpenedPageDB)
                    .enableAnnotationRendering(false)
                    .scrollHandle(DefaultScrollHandle(context))
                    .spacing(10)
                    .nightMode(false)
                    .pageSnap(true)
                    .onPageChange { page, _ ->
                        lastOpenedPageL = page
                    }
                    .load()

            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
