package com.example.bookReader.ui.theme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.artifex.mupdf.viewer.ContentInputStream
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.OutlineActivity
import com.example.bookReader.R
import com.example.bookReader.data.entity.ReadingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────────────────
// TOC tree helpers
// ─────────────────────────────────────────────────────────────────────────────

private data class TocNode(
    val item: OutlineActivity.Item,
    val depth: Int,
    val children: MutableList<TocNode> = mutableListOf()
)

private fun buildTocTree(outline: List<OutlineActivity.Item>): List<TocNode> {
    val nodes = outline.map { item ->
        val leading = item.title.length - item.title.trimStart().length
        val depth = leading / 4
        TocNode(OutlineActivity.Item(item.title.trimStart(), item.page), depth)
    }
    val roots = mutableListOf<TocNode>()
    val stack = mutableListOf<TocNode>()
    for (node in nodes) {
        while (stack.size > node.depth) stack.removeLast()
        if (stack.isEmpty()) roots.add(node) else stack.last().children.add(node)
        stack.add(node)
    }
    return roots
}

// ─────────────────────────────────────────────────────────────────────────────
// Main screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    bookId: Long,
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    pdfViewerViewModel: PdfViewerViewModel,
    bookStateViewModel: BookStateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val allBooks by libraryViewModel.allBooks.collectAsState()
    val book = remember(bookId, allBooks) { allBooks.find { it.bookId == bookId } }

    // ── State ─────────────────────────────────────────────────────────────────
    var core by remember { mutableStateOf<MuPDFCore?>(null) }
    var totalPages by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sessionStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var outline by remember { mutableStateOf<List<OutlineActivity.Item>?>(null) }

    val currentPage by pdfViewerViewModel.currentPage.collectAsState()
    val sessionState by pdfViewerViewModel.sessionState.collectAsState()

    var isChromeVisible by remember { mutableStateOf(true) }
    var showPageJumpDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showTocSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var jumpToPage by remember { mutableStateOf<Int?>(null) }
    var readerViewRef by remember { mutableStateOf<MuPdfReaderView?>(null) }

    // New feature states
    var linksEnabled by remember { mutableStateOf(true) }
    var horizontalScrolling by remember { mutableStateOf(true) }
    var showCopySheet by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when message is set
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // ── 1. Open document ──────────────────────────────────────────────────────
    LaunchedEffect(bookId, book) {
        if (book == null) { errorMessage = "Book not found"; isLoading = false; return@LaunchedEffect }
        isLoading = true; errorMessage = null
        try {
            withContext(Dispatchers.IO) {
                val uri = book.uri.toUri()
                val fileSize: Long = try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
                } catch (_: Exception) { -1L }
                val stream = ContentInputStream(context.contentResolver, uri, fileSize)
                val mupdfCore = MuPDFCore(stream, "application/pdf")
                val pages = mupdfCore.countPages()
                val toc: List<OutlineActivity.Item>? =
                    if (mupdfCore.hasOutline()) mupdfCore.getOutline() else null
                withContext(Dispatchers.Main) {
                    core = mupdfCore; totalPages = pages; outline = toc
                    val savedPage = bookStateViewModel.getBookState(bookId)?.currentPage ?: 0
                    pdfViewerViewModel.startSession(bookId = bookId, startPage = savedPage, totalPages = pages)
                    if (savedPage > 0) jumpToPage = savedPage
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error loading PDF: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // ── 2. Session timer ──────────────────────────────────────────────────────
    LaunchedEffect(sessionState) {
        if (sessionState != null) {
            while (true) {
                delay(1000L)
                pdfViewerViewModel.updateSessionTime(System.currentTimeMillis() - sessionStartTime)
            }
        }
    }

    // ── 3. Lifecycle ──────────────────────────────────────────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) sessionStartTime = System.currentTimeMillis()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(bookId) {
        onDispose { core?.onDestroy(); pdfViewerViewModel.endSession() }
    }

    BackHandler {
        when {
            showCopySheet -> showCopySheet = false
            showSearchBar -> { showSearchBar = false; searchQuery = ""; readerViewRef?.clearSearch() }
            showTocSheet -> showTocSheet = false
            else -> navController.navigateUp()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it }
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.sign_out_circle),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(24.dp)
                                )
                                }
                                Text(
                                    text = book?.title ?: "Loading…",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = {
                                    showSearchBar = !showSearchBar
                                    if (!showSearchBar) { searchQuery = ""; readerViewRef?.clearSearch() }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.search_alt),
                                        contentDescription = "Search",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                if (outline != null) {
                                    IconButton(onClick = { showTocSheet = true }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.sort),
                                            contentDescription = "Table of contents",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    linksEnabled = !linksEnabled
                                    readerViewRef?.setLinksEnabled(linksEnabled)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.AddLink,
                                        contentDescription = if (linksEnabled) "Disable links" else "Enable links",
                                        modifier = Modifier.size(24.dp),
                                        tint = if (linksEnabled)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                IconButton(onClick = {
                                    horizontalScrolling = !horizontalScrolling
                                    readerViewRef?.setScrollHorizontal(horizontalScrolling)
                                }) {
                                    Icon(
                                        imageVector = if (horizontalScrolling)
                                            Icons.Rounded.ArrowBack
                                        else
                                            Icons.Rounded.ArrowUpward,
                                        contentDescription = if (horizontalScrolling)
                                            "Switch to vertical scroll"
                                        else
                                            "Switch to horizontal scroll",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = { showPageJumpDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.NorthEast,
                                        contentDescription = "Jump to page",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = { showGoalDialog = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.fluent_timer_12_regular),
                                        contentDescription = "Reading goal",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                        AnimatedVisibility(
                            visible = showSearchBar,
                            enter = slideInVertically { -it },
                            exit = slideOutVertically { -it }
                        ) {
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearchForward = {
                                    if (searchQuery.isNotBlank())
                                        readerViewRef?.search(searchQuery, direction = +1)
                                },
                                onSearchBackward = {
                                    if (searchQuery.isNotBlank())
                                        readerViewRef?.search(searchQuery, direction = -1)
                                },
                                onClose = {
                                    showSearchBar = false
                                    searchQuery = ""
                                    readerViewRef?.clearSearch()
                                },
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isChromeVisible && sessionState != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
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
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                errorMessage != null -> ErrorState(
                    message = errorMessage!!,
                    onBack = { navController.navigateUp() },
                    modifier = Modifier.align(Alignment.Center)
                )

                core != null -> {
                    AndroidView(
                        factory = { ctx ->
                            MuPdfReaderView(
                                context = ctx,
                                core = core!!,
                                onPageChanged = { page ->
                                    pdfViewerViewModel.updatePage(page)
                                    bookStateViewModel.updateBookState(
                                        bookId = bookId,
                                        currentPage = page,
                                        status = if (page >= totalPages - 1)
                                            ReadingStatus.COMPLETED
                                        else
                                            ReadingStatus.READING
                                    )
                                },
                                onChromeTap = { isChromeVisible = !isChromeVisible },
                                onLongPress = { showCopySheet = true }
                            ).also { view ->
                                view.layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                                view.setLinksEnabled(linksEnabled)
                                view.setScrollHorizontal(horizontalScrolling)
                                readerViewRef = view
                            }
                        },
                        update = { view ->
                            jumpToPage?.let { page ->
                                view.setDisplayedViewIndex(page)
                                jumpToPage = null
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // ── Dialogs ───────────────────────────────────────────────────────────
        if (showPageJumpDialog) {
            PageJumpDialog(
                currentPage = currentPage,
                totalPages = totalPages,
                onDismiss = { showPageJumpDialog = false },
                onPageSelected = { page -> jumpToPage = page; showPageJumpDialog = false }
            )
        }
        if (showGoalDialog) {
            ReadingGoalDialog(
                currentGoal = sessionState?.dailyGoalMinutes,
                onDismiss = { showGoalDialog = false },
                onGoalSet = { minutes -> pdfViewerViewModel.setReadingGoal(bookId, minutes); showGoalDialog = false }
            )
        }
        if (showTocSheet && outline != null) {
            TocBottomSheet(
                outline = outline!!,
                currentPage = currentPage,
                onPageSelected = { page -> jumpToPage = page; showTocSheet = false },
                onDismiss = { showTocSheet = false }
            )
        }
        // Text copy bottom sheet (triggered by long-press on the PDF)
        if (showCopySheet) {
            CopyTextSheet(
                onCopyPage = {
                    showCopySheet = false
                    scope.launch(Dispatchers.IO) {
                        val text = readerViewRef?.getCurrentPageText() ?: ""
                        withContext(Dispatchers.Main) {
                            if (text.isBlank()) {
                                snackbarMessage = "No selectable text on this page"
                            } else {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("PDF page text", text))
                                snackbarMessage = "Page text copied to clipboard"
                            }
                        }
                    }
                },
                onDismiss = { showCopySheet = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchForward: () -> Unit,
    onSearchBackward: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search in document…", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onSearchBackward) {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Previous result",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onSearchForward) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Next result",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.close_ring),
                    contentDescription = "Close search",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Copy text bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CopyTextSheet(
    onCopyPage: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Text Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCopyPage)
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.sort),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Copy page text",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Copies all selectable text from this page",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Collapsible TOC bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TocBottomSheet(
    outline: List<OutlineActivity.Item>,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val tocTree = remember(outline) { buildTocTree(outline) }
    val expandedNodes = remember { mutableStateOf(setOf<Int>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Text(
            text = "Table of Contents",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.CenterHorizontally),
            color = Color.Black
        )
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            tocTree.forEachIndexed { rootIdx, rootNode ->
                val isExpanded = expandedNodes.value.contains(rootIdx)
                item(key = "root_$rootIdx") {
                    TocRow(
                        title = rootNode.item.title,
                        page = rootNode.item.page,
                        depth = 0,
                        isCurrentChapter = rootNode.item.page == currentPage,
                        hasChildren = rootNode.children.isNotEmpty(),
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedNodes.value = if (isExpanded)
                                expandedNodes.value - rootIdx
                            else
                                expandedNodes.value + rootIdx
                        },
                        onPageSelected = onPageSelected
                    )
                }
                if (isExpanded) {
                    rootNode.children.forEachIndexed { childIdx, childNode ->
                        item(key = "child_${rootIdx}_$childIdx") {
                            TocRow(
                                title = childNode.item.title,
                                page = childNode.item.page,
                                depth = 1,
                                isCurrentChapter = childNode.item.page == currentPage,
                                hasChildren = false,
                                isExpanded = false,
                                onToggle = {},
                                onPageSelected = onPageSelected
                            )
                        }
                        // Grandchildren (depth 2)
                        childNode.children.forEachIndexed { grandIdx, grandNode ->
                            item(key = "grand_${rootIdx}_${childIdx}_$grandIdx") {
                                TocRow(
                                    title = grandNode.item.title,
                                    page = grandNode.item.page,
                                    depth = 2,
                                    isCurrentChapter = grandNode.item.page == currentPage,
                                    hasChildren = false,
                                    isExpanded = false,
                                    onToggle = {},
                                    onPageSelected = onPageSelected
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun TocRow(
    title: String,
    page: Int,
    depth: Int,
    isCurrentChapter: Boolean,
    hasChildren: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onPageSelected: (Int) -> Unit,
) {
    val indent = (depth * 20).dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrentChapter)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.onBackground
            )
            .clickable {
                if (hasChildren) onToggle()
                else onPageSelected(page)
            }
            .padding(start = 16.dp + indent, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Expand/collapse arrow for parents, dot for leaves
            if (hasChildren) {
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Rounded.KeyboardArrowDown
                    else
                        Icons.Rounded.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
            } else {
                Spacer(Modifier.width(24.dp))
            }
            Text(
                text = title,
                style = if (depth == 0)
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                else
                    MaterialTheme.typography.bodySmall,
                color = if (isCurrentChapter)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${page + 1}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    HorizontalDivider(thickness = 0.5.dp)
}

// ─────────────────────────────────────────────────────────────────────────────
// Reading progress bottom bar
// ─────────────────────────────────────────────────────────────────────────────

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
        color = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page ${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
                Text(
                    text = "${((currentPage.toFloat() / totalPages.coerceAtLeast(1)) * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }
            LinearProgressIndicator(
                progress = { currentPage.toFloat() / totalPages.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Session: ${formatReadingTime(sessionTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (goalProgress > 0f) {
                    Text(
                        text = if (isGoalMet) "Goal Met ✓" else "Goal: ${(goalProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isGoalMet) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorState(message: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onBack) { Text("Go Back") }
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
        title = { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text("Jump to Page") } },
        text = {
            OutlinedTextField(
                value = pageInput,
                onValueChange = { pageInput = it.filter { c -> c.isDigit() } },
                label = { Text("Page number (1–$totalPages)") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            TextButton(onClick = {
                val page = pageInput.toIntOrNull()
                if (page != null && page in 1..totalPages) onPageSelected(page - 1)
            }) { Text("Go") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ReadingGoalDialog(
    currentGoal: Int?,
    onDismiss: () -> Unit,
    onGoalSet: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf(currentGoal?.toString() ?: "30") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text("Daily Reading Goal") } },
        text = {
            Column {
                Text("How many minutes do you want to read today?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Minutes per day") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            TextButton(onClick = {
                val minutes = goalInput.toIntOrNull()
                if (minutes != null && minutes > 0) onGoalSet(minutes)
            }) { Text("Set Goal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatReadingTime(ms: Long): String {
    val s = (ms / 1000) % 60
    val m = (ms / 60_000) % 60
    val h = ms / 3_600_000
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}