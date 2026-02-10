@file:Suppress("SetJavaScriptEnabled")
package com.example.bookReader.ui.theme

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.artifex.mupdf.viewer.*

private fun Context.openCore(uri: Uri): MuPDFCore {
    val input = contentResolver.openInputStream(uri)!!
    val bytes = input.readBytes()
    input.close()
    return MuPDFCore(bytes, uri.toString())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    uri: Uri,

) {

    val context = LocalContext.current

    val core = remember(uri) {
        context.openCore(uri)
    }

    var readerView by remember { mutableStateOf<ReaderView?>(null) }

    val startTime = remember { System.currentTimeMillis() }

    DisposableEffect(Unit) {
        onDispose {
            core.onDestroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Reader") },
                navigationIcon = {
                },
                actions = {
                    IconButton(onClick = {
                    }) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = {
                    readerView?.smartMoveBackwards()
                }) {
                    Icon(Icons.Default.AccountBox, null)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    readerView?.smartMoveForwards()
                }) {
                    Icon(Icons.Default.Build, null)
                }
            }
        }
    ) { padding ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { ctx ->
                ReaderView(ctx).apply {
                    setAdapter(PageAdapter(ctx, core))
                    readerView = this
                }
            }
        )
    }
}