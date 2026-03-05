package com.example.bookReader.ui

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.artifex.mupdf.viewer.ContentInputStream
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.OutlineActivity
import com.example.bookReader.R
import com.example.bookReader.data.entity.ReadingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        val depth   = leading / 4
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
// Theme definitions (UI metadata only — ColorMatrix lives in MuPdfReaderView)
// ─────────────────────────────────────────────────────────────────────────────

private val ThemeList = PdfTheme.entries          // NORMAL, SEPIA, DARK_SEPIA, NIGHT

/** Returns the background colour that should tint the Scaffold behind the PDF. */
private fun PdfTheme.scaffoldBg(): Color = when (this) {
    PdfTheme.NORMAL    -> Color(0xFFF5F5F5)
    PdfTheme.SEPIA     -> Color(0xFFF4ECD8)
    PdfTheme.DARK_SEPIA -> Color(0xFF2B2016)
    PdfTheme.NIGHT     -> Color(0xFF1A1A1A)
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
    val context       = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope          = rememberCoroutineScope()

    val allBooks by libraryViewModel.allBooks.collectAsState()
    val book = remember(bookId, allBooks) { allBooks.find { it.bookId == bookId } }

    // ── Core state ────────────────────────────────────────────────────────────
    var core         by remember { mutableStateOf<MuPDFCore?>(null) }
    var totalPages   by remember { mutableStateOf(0) }
    var isLoading    by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // BUG FIX #6: The old single `sessionStartTime` was reset to System.currentTimeMillis()
    // on every ON_RESUME, which wiped out all time accumulated before the app was backgrounded.
    // We now keep a separate `accumulatedSessionTime` that is updated on pause, so the timer
    // resumes from where it left off rather than restarting from zero.
    var sessionPeriodStart by remember { mutableStateOf(System.currentTimeMillis()) }
    var accumulatedSessionTime by remember { mutableStateOf(0L) }
    var outline      by remember { mutableStateOf<List<OutlineActivity.Item>?>(null) }

    val currentPage  by pdfViewerViewModel.currentPage.collectAsState()
    val sessionState by pdfViewerViewModel.sessionState.collectAsState()

    // ── UI toggles ────────────────────────────────────────────────────────────
    var isChromeVisible   by remember { mutableStateOf(true) }
    var showPageJumpDialog by remember { mutableStateOf(false) }
    var showGoalDialog    by remember { mutableStateOf(false) }
    var showTocSheet      by remember { mutableStateOf(false) }
    var showSearchBar     by remember { mutableStateOf(false) }
    var searchQuery       by remember { mutableStateOf("") }
    var jumpToPage        by remember { mutableStateOf<Int?>(null) }
    var readerViewRef     by remember { mutableStateOf<MuPdfReaderView?>(null) }

    // ── Feature states ────────────────────────────────────────────────────────
    var linksEnabled       by remember { mutableStateOf(false) }
    var horizontalScrolling by remember { mutableStateOf(true) }
    var currentTheme       by remember { mutableStateOf(PdfTheme.NORMAL) }
    var showThemeSheet     by remember { mutableStateOf(false) }
    var showThemes     by remember { mutableStateOf(false) }

    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val correctedPadding = (imeBottom - bottomBarHeight).coerceAtLeast(0.dp)
    // ── Snackbar ──────────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage   by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it); snackbarMessage = null }
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
                val stream     = ContentInputStream(context.contentResolver, uri, fileSize)
                val mupdfCore  = MuPDFCore(stream, "application/pdf")
                val pages      = mupdfCore.countPages()
                val toc        = if (mupdfCore.hasOutline()) mupdfCore.getOutline() else null
                withContext(Dispatchers.Main) {
                    core = mupdfCore; totalPages = pages; outline = toc
                    val savedPage = bookStateViewModel.getBookState(bookId)?.currentPage ?: 0
                    pdfViewerViewModel.startSession(bookId, savedPage, pages)
                    if (savedPage > 0) jumpToPage = savedPage
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error loading PDF: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // ── 2. Session timer + midnight reset ────────────────────────────────────
    // Keyed on isSessionActive so page changes (which update sessionState) don't
    // restart the coroutine and reset the period-start timestamp mid-session.
    val isSessionActive = sessionState != null
    LaunchedEffect(isSessionActive) {
        if (isSessionActive) {
            sessionPeriodStart    = System.currentTimeMillis()
            accumulatedSessionTime = 0L

            // Snapshot the day we started on.  If the clock crosses midnight while
            // the user is still reading we detect it below and fire onMidnightCrossed.
            var trackedDayStart = pdfViewerViewModel.todayStartMs()

            while (true) {
                delay(1000L)

                val newDayStart = pdfViewerViewModel.todayStartMs()
                if (newDayStart != trackedDayStart) {
                    // ── Midnight crossed ──────────────────────────────────────
                    // 1. Compute total minutes read on the day that just ended.
                    val totalMsBeforeMidnight =
                        accumulatedSessionTime + (System.currentTimeMillis() - sessionPeriodStart)
                    val minutesBefore = totalMsBeforeMidnight / 60_000L

                    // 2. Notify the ViewModel so it can persist the result and
                    //    reset its own todayReadingTimeMs / sessionTimeSpent.
                    pdfViewerViewModel.onMidnightCrossed(trackedDayStart, minutesBefore)

                    // 3. Reset the local timer accumulators for the new day.
                    accumulatedSessionTime = 0L
                    sessionPeriodStart     = System.currentTimeMillis()
                    trackedDayStart        = newDayStart
                }

                pdfViewerViewModel.updateSessionTime(
                    accumulatedSessionTime + (System.currentTimeMillis() - sessionPeriodStart)
                )
            }
        }
    }

    // ── 3. Lifecycle ──────────────────────────────────────────────────────────
    // BUG FIX #6 (cont.): ON_PAUSE snapshots elapsed time into accumulatedSessionTime;
    // ON_RESUME resets the period-start clock so the timer continues from the snapshot.
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, ev ->
            when (ev) {
                Lifecycle.Event.ON_PAUSE ->
                    accumulatedSessionTime += System.currentTimeMillis() - sessionPeriodStart
                Lifecycle.Event.ON_RESUME ->
                    sessionPeriodStart = System.currentTimeMillis()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }
    DisposableEffect(bookId) {
        onDispose { core?.onDestroy(); pdfViewerViewModel.endSession() }
    }

    BackHandler {
        when {
            showSearchBar -> { showSearchBar = false; searchQuery = ""; readerViewRef?.clearSearch() }
            showTocSheet  -> showTocSheet = false
            showThemeSheet -> showThemeSheet = false
            else -> navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                isLoading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                errorMessage != null ->
                    ErrorState(errorMessage!!, { navController.navigateUp() },
                        modifier = Modifier.align(Alignment.Center))

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
                                            ReadingStatus.COMPLETED else ReadingStatus.READING
                                    )
                                },
                                onChromeTap = {
                                    isChromeVisible = !isChromeVisible
                                    showThemes = false
                                    showSearchBar = false
                                }
                            ).also { v ->
                                v.layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT)
                                v.setLinksEnabled(linksEnabled)
                                v.setScrollHorizontal(horizontalScrolling)
                                v.applyTheme(currentTheme)
                                readerViewRef = v
                            }
                        },
                        update = { view ->
                            jumpToPage?.let { page -> view.setDisplayedViewIndex(page); jumpToPage = null }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Box(
            modifier = modifier.align(Alignment.TopCenter)
        ){
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit  = fadeOut() + slideOutVertically { -it }
            ) {
                GeneralTopBar(
                    titleText = book?.title ?: "Loading…",
                    onBackClicked = {
                        navController.popBackStack()
                    }
                )
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ){
            Column() {
                if(showThemes) {
                    ThemeSelector(
                        onThemeSelected = { theme ->
                            currentTheme = theme
                            readerViewRef?.applyTheme(theme)
                            showThemes = false
                        },
                    )
                }
                if (showSearchBar)
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchForward = {
                            if (searchQuery.isNotBlank()) readerViewRef?.search(
                                searchQuery,
                                +1
                            )
                        },
                        onSearchBackward = {
                            if (searchQuery.isNotBlank()) readerViewRef?.search(
                                searchQuery,
                                -1
                            )
                        },
                        onClose = {
                            showSearchBar = false; searchQuery =
                            ""; readerViewRef?.clearSearch()
                        },
                        modifier = Modifier.padding(bottom = correctedPadding)
                    )
                AnimatedVisibility(
                    visible = isChromeVisible && sessionState != null,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .onSizeChanged {
                                bottomBarHeight = with(density) { it.height.toDp() }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column {
                                Row {
                                    FilledIconButton(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            if (showThemes) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.onBackground
                                        ),
                                        onClick = {
                                            if(showSearchBar) showSearchBar = false
                                            showThemes = !showThemes
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.paint_roller),
                                            contentDescription = "ColorPicker",
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(25.dp),
                                            tint = Color.Black
                                        )
                                    }
                                    // Search
                                    FilledIconButton(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            if (showSearchBar) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.onBackground
                                        ),
                                        onClick = {
                                            if(showThemes) showThemes = false
                                            showSearchBar = !showSearchBar
                                            if (!showSearchBar) {
                                                searchQuery = ""; readerViewRef?.clearSearch()
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.search_alt),
                                            contentDescription = "ColorPicker",
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(25.dp),
                                            tint = Color.Black
                                        )
                                    }

                                    if (outline != null)
                                        FilledIconButton(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                if (showTocSheet) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.onBackground
                                            ),
                                            onClick = { showTocSheet = true }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.sort),
                                                contentDescription = "TableOfContent",
                                                modifier = Modifier
                                                    .padding(3.dp)
                                                    .size(25.dp),
                                                tint = Color.Black
                                            )
                                        }

                                    FilledIconButton(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            if (linksEnabled) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.onBackground
                                        ),
                                        onClick = {
                                            linksEnabled = !linksEnabled
                                            readerViewRef?.setLinksEnabled(linksEnabled)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.link),
                                            contentDescription = "ColorPicker",
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(25.dp),
                                            tint = Color.Black
                                        )
                                    }
                                    IconButton(onClick = {
                                        horizontalScrolling = !horizontalScrolling
                                        readerViewRef?.setScrollHorizontal(horizontalScrolling)
                                    }) {
                                        Icon(
                                            if (horizontalScrolling) painterResource(R.drawable.expand_right_stop)
                                            else painterResource(R.drawable.expand_down_stop),
                                            if (horizontalScrolling) "Switch to vertical" else "Switch to horizontal",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    FilledIconButton(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            if (showPageJumpDialog) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.onBackground
                                        ),
                                        onClick = { showPageJumpDialog = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.external),
                                            contentDescription = "Jump page",
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(25.dp),
                                            tint = Color.Black
                                        )
                                    }
                                    FilledIconButton(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            if (showGoalDialog) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.onBackground
                                        ),
                                        onClick = { showGoalDialog = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.fluent_timer_12_regular),
                                            contentDescription = "Timer",
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(25.dp),
                                            tint = Color.Black
                                        )
                                    }
                                }
                                Row {
                                    ReadingProgressBar(
                                        currentPage = currentPage,
                                        totalPages = totalPages,
                                        sessionTime = sessionState?.sessionTimeSpent ?: 0L,
                                        goalProgress = pdfViewerViewModel.getGoalProgress(),
                                        isGoalMet = pdfViewerViewModel.isGoalMet()
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }


    if (showPageJumpDialog)
        PageJumpDialog(currentPage, totalPages,
            onDismiss = { showPageJumpDialog = false },
            onPageSelected = { jumpToPage = it; showPageJumpDialog = false })

    if (showGoalDialog)
        ReadingGoalDialog(
            currentGoal = sessionState?.dailyGoalMinutes,
            onDismiss = { showGoalDialog = false },
            onGoalSet = { pdfViewerViewModel.setReadingGoal(bookId, it); showGoalDialog = false })

    if (showTocSheet && outline != null)
        TocBottomSheet(outline!!, currentPage,
            onPageSelected = { jumpToPage = it; showTocSheet = false },
            onDismiss = { showTocSheet = false })

}

@Composable
fun ThemeSelector(
    onThemeSelected: (PdfTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val themes = ThemeList

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 7.dp)
    ){
        items(themes) { theme ->

            val bgColor = when (theme) {
                PdfTheme.NORMAL     -> Color(0xFFFFFFFF)
                PdfTheme.SEPIA      -> Color(0xFFFFF0EF)
                PdfTheme.DARK_SEPIA -> Color(0xFFBCA77F)
                PdfTheme.NIGHT      -> Color(0xFF000000)
            }

            Surface(
                border = BorderStroke(2.dp, Color.White),
                onClick = {  onThemeSelected(theme)  },
                shape = RoundedCornerShape(8.dp),
                color = bgColor,
                shadowElevation = 4.dp,
                modifier = Modifier.padding(5.dp).size(50.dp)
            ) {

            }
        }
    }
}

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
                placeholder = { Text("Search text", fontSize = 14.sp) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.onBackground,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onSearchBackward) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack
                    , "Previous", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onSearchForward) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Next", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.close_ring), "Close", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TocBottomSheet(
    outline: List<OutlineActivity.Item>,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val tocTree       = remember(outline) { buildTocTree(outline) }
    val expandedNodes = remember { mutableStateOf(setOf<Int>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Text(
            "Table of Contents",
            style    = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.CenterHorizontally),
            color = Color.Black
        )
        HorizontalDivider()
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            tocTree.forEachIndexed { rootIdx, rootNode ->
                val expanded = expandedNodes.value.contains(rootIdx)
                item(key = "r$rootIdx") {
                    TocRow(
                        title       = rootNode.item.title,
                        page        = rootNode.item.page,
                        depth       = 0,
                        isCurrent   = rootNode.item.page == currentPage,
                        hasChildren = rootNode.children.isNotEmpty(),
                        isExpanded  = expanded,
                        onToggle    = {
                            expandedNodes.value = if (expanded)
                                expandedNodes.value - rootIdx
                            else
                                expandedNodes.value + rootIdx
                        },
                        onSelect    = onPageSelected
                    )
                }
                if (expanded) {
                    rootNode.children.forEachIndexed { ci, child ->
                        item(key = "c${rootIdx}_$ci") {
                            TocRow(child.item.title, child.item.page, 1,
                                child.item.page == currentPage, false, false, {}, onPageSelected)
                        }
                        child.children.forEachIndexed { gi, grand ->
                            item(key = "g${rootIdx}_${ci}_$gi") {
                                TocRow(grand.item.title, grand.item.page, 2,
                                    grand.item.page == currentPage, false, false, {}, onPageSelected)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun TocRow(
    title: String, page: Int, depth: Int,
    isCurrent: Boolean, hasChildren: Boolean, isExpanded: Boolean,
    onToggle: () -> Unit, onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.onBackground
            )
            .clickable { if (hasChildren) onToggle() else onSelect(page) }
            .padding(start = 16.dp + (depth * 20).dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (hasChildren) {
                Icon(
                    if (isExpanded) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
            } else {
                Spacer(Modifier.width(24.dp))
            }
            Text(
                title,
                style = if (depth == 0)
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                else
                    MaterialTheme.typography.bodySmall,
                color = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
        Text("${page + 1}", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp))
    }
    HorizontalDivider(thickness = 0.5.dp)
}

@Composable
private fun ReadingProgressBar(
    currentPage: Int, totalPages: Int, sessionTime: Long,
    goalProgress: Float, isGoalMet: Boolean, modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Page ${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.bodySmall)
            Text("${((currentPage.toFloat() / totalPages.coerceAtLeast(1)) * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
        LinearProgressIndicator(
            progress = { currentPage.toFloat() / totalPages.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Session: ${formatReadingTime(sessionTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (goalProgress > 0f)
                Text(
                    if (isGoalMet) "Goal Met ✓" else "Goal: ${(goalProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGoalMet) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorState(message: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(message, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onBack) { Text("Go Back") }
    }
}

@Composable
private fun PageJumpDialog(
    currentPage: Int, totalPages: Int,
    onDismiss: () -> Unit, onPageSelected: (Int) -> Unit
) {
    var input by remember { mutableStateOf((currentPage + 1).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to Page", modifier = Modifier.fillMaxWidth()) },
        text = {
            OutlinedTextField(input, { input = it.filter(Char::isDigit) },
                label = { Text("Page (1–$totalPages)") }, singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.onBackground,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onBackground))
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            TextButton(onClick = {
                input.toIntOrNull()?.takeIf { it in 1..totalPages }?.let { onPageSelected(it - 1) }
            }) { Text("Go") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ReadingGoalDialog(
    currentGoal: Int?, onDismiss: () -> Unit, onGoalSet: (Int) -> Unit
) {
    var input by remember { mutableStateOf(currentGoal?.toString() ?: "30") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Reading Goal") },
        text = {
            OutlinedTextField(input, { input = it.filter(Char::isDigit) },
                label = { Text("Minutes per day") }, singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.onBackground,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onBackground)
            )
        },
        shape = RoundedCornerShape(8.dp),
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            TextButton(onClick = {
                input.toIntOrNull()?.takeIf { it > 0 }?.let { onGoalSet(it) }
            }) { Text("Set Goal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatReadingTime(ms: Long): String {
    val s = (ms / 1000) % 60
    val m = (ms / 60_000) % 60
    val h =  ms / 3_600_000
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}