package com.example.bookReader.ui.theme

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.artifex.mupdf.fitz.Document
import com.example.bookReader.R
import com.example.bookReader.data.entity.ReadingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    bookId: Long,
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    pdfViewerViewModel: PdfViewerViewModel,
    bookStateViewModel: BookStateViewModel,
    showTimeGoal: Boolean,
    onTimeGoalClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allBooks by libraryViewModel.allBooks.collectAsState()
    val book = remember(bookId, allBooks) { allBooks.find { it.bookId == bookId } }

    var pdfDocument by remember { mutableStateOf<Document?>(null) }
    var totalPages by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var savedPage by remember { mutableStateOf(0) }

    val sessionState by pdfViewerViewModel.sessionState.collectAsState()
    val currentPage by pdfViewerViewModel.currentPage.collectAsState()

    var showPageJumpDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    // Get saved reading progress from BookStateViewModel
    LaunchedEffect(bookId) {
        try {
            val bookState = bookStateViewModel.getBookState(bookId)
            savedPage = bookState?.currentPage ?: 0
        } catch (e: Exception) {
            savedPage = 0
        }
    }

    // Timer for reading session
    LaunchedEffect(sessionState) {
        if (sessionState != null) {
            while (true) {
                delay(1000) // Update every second
                val elapsed = System.currentTimeMillis() - (sessionState?.startTime ?: 0L)
                pdfViewerViewModel.updateSessionTime(elapsed)
            }
        }
    }

    // Load PDF document
    LaunchedEffect(bookId, book) {
        if (book == null) {
            errorMessage = "Book not found"
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null

        try {
            withContext(Dispatchers.IO) {
                // Get PDF input stream
                val inputStream = libraryViewModel.openPdf(bookId)
                if (inputStream == null) {
                    errorMessage = "Failed to open PDF"
                    return@withContext
                }

                // Create a temporary file for MuPDF
                val tempFile = File(context.cacheDir, "temp_pdf_$bookId.pdf")
                FileOutputStream(tempFile).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                // Open with MuPDF
                val bytes = tempFile.readBytes()
                val document = Document.openDocument(bytes, "application/pdf")

                pdfDocument = document
                totalPages = document.countPages()

                // Start reading session with saved page
                withContext(Dispatchers.Main) {
                    pdfViewerViewModel.startSession(
                        bookId = bookId,
                        startPage = savedPage,
                        totalPages = totalPages
                    )
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error loading PDF: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Cleanup
    DisposableEffect(bookId) {
        onDispose {
            pdfDocument?.destroy()
            pdfViewerViewModel.endSession()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book?.title ?: "Loading...",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = "Page ${currentPage + 1} of $totalPages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Jump to page
                    IconButton(onClick = { showPageJumpDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Jump to page"
                        )
                    }

                    // Reading goal
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Reading goal"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (sessionState != null) {
                ReadingProgressBar(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    sessionTime = sessionState?.sessionTimeSpent ?: 0L,
                    goalProgress = pdfViewerViewModel.getGoalProgress(),
                    isGoalMet = pdfViewerViewModel.isGoalMet()
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text("Go Back")
                        }
                    }
                }
                pdfDocument != null -> {
                    PdfPageViewer(
                        document = pdfDocument!!,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageChanged = { newPage ->
                            pdfViewerViewModel.updatePage(newPage)
                            // Update progress using BookStateViewModel
                            bookStateViewModel.updateBookState(
                                bookId = bookId,
                                currentPage = newPage,
                                status = if (newPage >= totalPages - 1) ReadingStatus.COMPLETED
                                else ReadingStatus.READING
                            )
                        }
                    )
                }
            }
        }

        // Page Jump Dialog
        if (showPageJumpDialog) {
            PageJumpDialog(
                currentPage = currentPage,
                totalPages = totalPages,
                onDismiss = { showPageJumpDialog = false },
                onPageSelected = { page ->
                    pdfViewerViewModel.updatePage(page)
                    showPageJumpDialog = false
                }
            )
        }

        // Reading Goal Dialog
        if (showGoalDialog) {
            ReadingGoalDialog(
                bookId = bookId,
                currentGoal = sessionState?.dailyGoalMinutes,
                onDismiss = { showGoalDialog = false },
                onGoalSet = { minutes ->
                    pdfViewerViewModel.setReadingGoal(bookId, minutes)
                    showGoalDialog = false
                }
            )
        }
    }
}

@Composable
private fun PdfPageViewer(
    document: Document,
    currentPage: Int,
    totalPages: Int,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentPage)

    // Track page changes
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex != currentPage) {
            onPageChanged(listState.firstVisibleItemIndex)
        }
    }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
    ) {
        items(totalPages) { pageIndex ->
            PdfPage(
                document = document,
                pageIndex = pageIndex,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY
            )
        }
    }
}

@Composable
private fun PdfPage(
    document: Document,
    pageIndex: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pageIndex) {
        scope.launch(Dispatchers.IO) {
            try {
                val page = document.loadPage(pageIndex)
                val bounds = page.getBounds()
                val pageWidth = bounds.x1 - bounds.x0
                val pageHeight = bounds.y1 - bounds.y0

                // Calculate bitmap dimensions
                val dpi = 150f
                val renderScale = dpi / 72f
                val width = (pageWidth * renderScale).toInt()
                val height = (pageHeight * renderScale).toInt()

                // Create bitmap
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                // Create Android DrawDevice for rendering
                val matrix = com.artifex.mupdf.fitz.Matrix(renderScale)
                val androidDevice = com.artifex.mupdf.fitz.android.AndroidDrawDevice(bmp, 0, 0, width, height, 0, width)

                try {
                    // Run the page through the device
                    page.run(androidDevice, matrix, null)
                } finally {
                    androidDevice.destroy()
                }

                withContext(Dispatchers.Main) {
                    bitmap = bmp
                }

                page.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.707f) // A4 aspect ratio
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { bmp ->
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                drawImage(
                    image = bmp.asImageBitmap(),
                    topLeft = Offset.Zero
                )
            }
        } ?: CircularProgressIndicator()
    }
}

@Composable
private fun ReadingProgressBar(
    currentPage: Int,
    totalPages: Int,
    sessionTime: Long,
    goalProgress: Float,
    isGoalMet: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Reading progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${((currentPage.toFloat() / totalPages) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            LinearProgressIndicator(
                progress = currentPage.toFloat() / totalPages,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Session time and goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Session: ${formatTime(sessionTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (goalProgress > 0) {
                    Text(
                        text = if (isGoalMet) "Goal Met! ✓" else "Goal: ${(goalProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isGoalMet) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PageJumpDialog(
    currentPage: Int,
    totalPages: Int,
    onDismiss: () -> Unit,
    onPageSelected: (Int) -> Unit
) {
    var pageInput by remember { mutableStateOf((currentPage + 1).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to Page") },
        text = {
            OutlinedTextField(
                value = pageInput,
                onValueChange = { pageInput = it },
                label = { Text("Page number (1-$totalPages)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val page = pageInput.toIntOrNull()
                    if (page != null && page in 1..totalPages) {
                        onPageSelected(page - 1)
                    }
                }
            ) {
                Text("Go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ReadingGoalDialog(
    bookId: Long,
    currentGoal: Int?,
    onDismiss: () -> Unit,
    onGoalSet: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf(currentGoal?.toString() ?: "30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Reading Goal") },
        text = {
            Column {
                Text("Set your daily reading goal in minutes")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    label = { Text("Minutes per day") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = goalInput.toIntOrNull()
                    if (minutes != null && minutes > 0) {
                        onGoalSet(minutes)
                    }
                }
            ) {
                Text("Set Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = milliseconds / (1000 * 60 * 60)

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%d:%02d", minutes, seconds)
    }
}