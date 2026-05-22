package com.timepass.bookreader.ui.pdfviewer

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artifex.mupdf.viewer.ContentInputStream
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.OutlineActivity
import com.timepass.bookreader.R
import com.timepass.bookreader.ui.TopBar
import com.timepass.bookreader.ui.home.BookStateViewModel
import com.timepass.bookreader.ui.home.Button
import com.timepass.bookreader.ui.home.LibraryViewModel
import com.timepass.bookreader.ui.home.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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


private val ThemeList = PdfTheme.entries

private fun PdfTheme.scaffoldBg(): Color = when (this) {
    PdfTheme.NORMAL    -> Color(0xFFF5F5F5)
    PdfTheme.SEPIA     -> Color(0xFFF4ECD8)
    PdfTheme.DARK_SEPIA -> Color(0xFF2B2016)
    PdfTheme.NIGHT     -> Color(0xFF1A1A1A)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    onBack: () -> Unit,
    bookId: Long,
    libraryViewModel: LibraryViewModel,
    pdfViewerViewModel: PdfViewerViewModel,
    bookStateViewModel: BookStateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val allBooks by libraryViewModel.allBooks.collectAsState()
    val book = remember(bookId, allBooks) { allBooks.find { it.bookId == bookId } }

    var core by remember { mutableStateOf<MuPDFCore?>(null) }
    var totalPages by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sessionPeriodStart by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var accumulatedSessionTime by remember { mutableLongStateOf(0L) }
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

    var linksEnabled by remember { mutableStateOf(false) }
    var horizontalScrolling by remember { mutableStateOf(true) }
    var currentTheme by remember { mutableStateOf(PdfTheme.NORMAL) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showThemes by remember { mutableStateOf(false) }

    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val correctedPadding = (imeBottom - bottomBarHeight).coerceAtLeast(0.dp)
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { snackbarHostState.showSnackbar(it); snackbarMessage = null }
    }

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

    val isSessionActive = sessionState != null
    LaunchedEffect(isSessionActive) {
        if (isSessionActive) {
            sessionPeriodStart = System.currentTimeMillis()
            accumulatedSessionTime = 0L
            while (true) {
                delay(1000L)
                pdfViewerViewModel.updateSessionTime(
                    accumulatedSessionTime + (System.currentTimeMillis() - sessionPeriodStart)
                )
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, ev ->
            when (ev) {
                Lifecycle.Event.ON_PAUSE -> {
                    accumulatedSessionTime += System.currentTimeMillis() - sessionPeriodStart
                }
                Lifecycle.Event.ON_STOP -> {
                    pdfViewerViewModel.endSessionBlocking()
                }
                Lifecycle.Event.ON_START -> {
                    if (totalPages > 0) {
                        pdfViewerViewModel.startSession(bookId, currentPage, totalPages)
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    sessionPeriodStart = System.currentTimeMillis()
                }
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
            else -> onBack()
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
                    ErrorState(errorMessage!!, { onBack() },
                        modifier = Modifier.align(Alignment.Center))

                core != null -> {
                    AndroidView(
                        factory = { ctx ->
                            MuPdfReaderView(
                                context = ctx,
                                core = core!!,
                                onPageChanged = { page ->
                                    pdfViewerViewModel.updatePage(page)
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
        ) {
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { it / 3 },
                    animationSpec = tween(180)
                ) + fadeIn(
                    animationSpec = tween(120)
                ),

                exit = slideOutHorizontally(
                    targetOffsetX = { it / 4 },
                    animationSpec = tween(180)
                ) + fadeOut(
                    animationSpec = tween(120)
                )
            ) {
                TopBar(
                    onActionClicked = onBack,
                    icon = R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24,
                    titleText = book?.title ?: "Loding",
                    modifier = modifier.align(Alignment.TopCenter)
                )
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ){
            Column {
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
                            if (searchQuery.isNotBlank())
                                readerViewRef?.search(searchQuery, +1) { page ->
                                    jumpToPage = page
                                }
                        },
                        onSearchBackward = {
                            if (searchQuery.isNotBlank())
                                readerViewRef?.search(searchQuery, -1) { page ->
                                    jumpToPage = page
                                }
                        },
                        onClose = {
                            showSearchBar = false; searchQuery =
                            ""; readerViewRef?.clearSearch()
                        },
                        modifier = Modifier.padding(bottom = correctedPadding)
                    )
                AnimatedVisibility (
                    visible = isChromeVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { it / 3 },
                        animationSpec = tween(180)
                    ) + fadeIn(
                        animationSpec = tween(120)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                        animationSpec = tween(180)
                    ) + fadeOut(
                        animationSpec = tween(120)
                    )
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary),
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
                                    Button(
                                        isActive = showThemes,
                                        onClick = {
                                            if (showSearchBar) showSearchBar = false
                                            showThemes = !showThemes
                                        },
                                        icon = R.drawable.format_paint_24dp_000000_fill0_wght300_grad0_opsz24,
                                        contentDescription = "ColorPicker",
                                    )
                                    Button(
                                        isActive = showSearchBar,
                                        onClick = {
                                            if (showThemes) showThemes = false
                                            showSearchBar = !showSearchBar
                                            if (!showSearchBar) {
                                                searchQuery = ""; readerViewRef?.clearSearch()
                                            }
                                        },
                                        icon = R.drawable.search_24dp_000000_fill0_wght300_grad0_opsz24,
                                        contentDescription = "search",
                                    )
                                    Button(
                                        isActive = showTocSheet,
                                        onClick = {
                                            showTocSheet = true
                                        },
                                        icon = R.drawable.sort_24dp_000000_fill0_wght300_grad0_opsz24,
                                        contentDescription = "toc",
                                    )
                                    Button(
                                        isActive = linksEnabled,
                                        onClick = {
                                            linksEnabled = !linksEnabled
                                            readerViewRef?.setLinksEnabled(linksEnabled)
                                        },
                                        icon = R.drawable.link_24dp_000000_fill0_wght300_grad0_opsz24,
                                        contentDescription = "link",
                                    )
                                    Button(
                                        isActive = horizontalScrolling,
                                        onClick = {
                                            horizontalScrolling = !horizontalScrolling
                                            readerViewRef?.setScrollHorizontal(horizontalScrolling)
                                        },
                                        icon = R.drawable.horizontal_align_right_24dp_000000_fill0_wght300_grad0_opsz24,
                                        contentDescription = "horizontal scroll",
                                    )
                                    Button(
                                        isActive = showPageJumpDialog,
                                        onClick = {
                                            showPageJumpDialog = true
                                        },
                                        icon = R.drawable.article_shortcut_24dp_000000_fill0_wght300_grad0_opsz24,
                                        contentDescription = "jump to page",
                                    )
                                    Button(
                                        isActive = showGoalDialog,
                                        onClick = {
                                            showGoalDialog = true
                                        },
                                        icon = R.drawable.alarm_24dp_000000_fill0_wght300_grad0_opsz24,
                                        contentDescription = "timer",
                                    )
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

    val jumpTextFieldState = rememberTextFieldState()
    val goalTextFieldState = rememberTextFieldState()

    LaunchedEffect(currentPage) {
        jumpTextFieldState.setTextAndPlaceCursorAtEnd(currentPage.toString())
    }

    LaunchedEffect(sessionState?.dailyGoalMinutes) {
        goalTextFieldState.setTextAndPlaceCursorAtEnd(
            sessionState?.dailyGoalMinutes?.toString() ?: ""
        )
    }

    if (showPageJumpDialog)
        DialogBox(
            textFieldState = jumpTextFieldState,
            textFieldLabel = "Page (1–$totalPages)",
            onDismiss = { showPageJumpDialog = false },
            dismissText = "Cancel",
            confirmText = "Go",
            heading = "Jump to page",
            keyboardType = KeyboardType.Number,
            onConfirm = {
                val page = jumpTextFieldState.text.toString()
                    .toIntOrNull()

                if (page != null && page in 1..totalPages) {
                    jumpToPage = page
                }

                showPageJumpDialog = false
            }
        )

    if (showGoalDialog)
        DialogBox(
            textFieldState = goalTextFieldState,
            textFieldLabel = "Minutes",
            onDismiss = { showGoalDialog = false },
            confirmText = "Set Goal",
            keyboardType = KeyboardType.Number,
            dismissText = "Cancel",
            onConfirm = {
                goalTextFieldState.text.toString()
                    .toIntOrNull()
                    ?.takeIf { it > 0 }
                    ?.let { goal ->
                        pdfViewerViewModel.setReadingGoal(bookId, goal)
                    }

                showGoalDialog = false
            },
            heading = "Daily Reading Goal"
        )


    if (showTocSheet && outline != null)
        TocBottomSheet(
            outline!!,
            currentPage,
            onPageSelected = { jumpToPage = it; showTocSheet = false },
            onDismiss = { showTocSheet = false }
        )

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
                PdfTheme.NORMAL -> Color(0xFFFFFFFF)
                PdfTheme.SEPIA -> Color(0xFFFFF0EF)
                PdfTheme.DARK_SEPIA -> Color(0xFFBCA77F)
                PdfTheme.NIGHT -> Color(0xFF000000)
            }

            Surface(
                border = BorderStroke(2.dp, Color.White),
                onClick = {  onThemeSelected(theme)  },
                shape = RoundedCornerShape(8.dp),
                color = bgColor,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .padding(5.dp)
                    .size(50.dp)
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
        color = MaterialTheme.colorScheme.background,
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
                placeholder = { Text("Search text") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onSearchBackward) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24dp_000000_fill0_wght300_grad0_opsz24),
                    contentDescription = "Previous",
                )
            }
            IconButton(onClick = onSearchForward) {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward_24dp_000000_fill0_wght300_grad0_opsz24),
                    contentDescription = "Next",
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.close_24dp_000000_fill0_wght300_grad0_opsz24),
                    contentDescription = "Close",
                )
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
    val tocTree = remember(outline) { buildTocTree(outline) }
    val expandedNodes = remember { mutableStateOf(setOf<Int>()) }

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        Text(
            "Table of Contents",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.CenterHorizontally),
        )
        HorizontalDivider()
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            tocTree.forEachIndexed { rootIdx, rootNode ->
                val expanded = expandedNodes.value.contains(rootIdx)
                item(key = "r$rootIdx") {
                    TocRow(
                        title = rootNode.item.title,
                        page = rootNode.item.page,
                        depth = 0,
                        isCurrent = rootNode.item.page == currentPage,
                        hasChildren = rootNode.children.isNotEmpty(),
                        isExpanded = expanded,
                        onToggle = {
                            expandedNodes.value = if (expanded)
                                expandedNodes.value - rootIdx
                            else
                                expandedNodes.value + rootIdx
                        },
                        onSelect = onPageSelected
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
                else MaterialTheme.colorScheme.surface
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
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.tertiary
        )
}

@Composable
private fun ReadingProgressBar(
    currentPage: Int,
    totalPages: Int,
    sessionTime: Long,
    goalProgress: Float, isGoalMet: Boolean,
    modifier: Modifier = Modifier
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
        ProgressBar(
            value = currentPage.toFloat() / totalPages.coerceAtLeast(1),
            frontColor = MaterialTheme.colorScheme.outline,
            backColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(vertical = 5.dp)
        )
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
fun DialogBox(
    onDismiss: () -> Unit,
    dismissText: String,
    confirmText: String,
    heading: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState? = null,
    textFieldLabel: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    content: (@Composable () -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleMedium
                )

                if (content != null) {
                    content()
                }

                if (textFieldState != null) {
                    OutlinedTextField(
                        state = textFieldState,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType
                        ),
                        label = if (textFieldLabel.isNotBlank()) {
                            {
                                Text(textFieldLabel)
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.Right
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable{ onDismiss() }
                    ) {
                        Text( text = dismissText, modifier = Modifier.padding(10.dp) )
                    }
                    Spacer( modifier = Modifier.width(10.dp) )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable{ onConfirm() }
                    ) {
                        Text( text = confirmText, modifier = Modifier.padding(10.dp) )
                    }
                }
            }
        }
    }
}

private fun formatReadingTime(ms: Long): String {
    val s = (ms / 1000) % 60
    val m = (ms / 60_000) % 60
    val h =  ms / 3_600_000
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}