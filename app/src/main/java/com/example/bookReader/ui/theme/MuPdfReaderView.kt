package com.example.bookReader.ui.theme

import android.content.Context
import android.view.MotionEvent
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.PageAdapter
import com.artifex.mupdf.viewer.ReaderView
import com.artifex.mupdf.viewer.SearchTask
import com.artifex.mupdf.viewer.SearchTaskResult

/**
 * Bridges MuPDF's View-based rendering into Compose-friendly callbacks.
 *
 * Features:
 *  - Page-change callbacks via [onPageChanged]
 *  - Chrome (toolbar) toggle via [onChromeTap]
 *  - Long-press callback via [onLongPress] (used for text copy)
 *  - Runtime scroll-direction switching via [setScrollHorizontal]
 *  - Link highlighting toggle via [setLinksEnabled] (inherited from ReaderView)
 *  - In-document search via [search] / [clearSearch]
 */
class MuPdfReaderView(
    context: Context,
    val core: MuPDFCore,
    /** Fired whenever the reader settles on a new page (0-based). */
    var onPageChanged: (page: Int) -> Unit = {},
    /** Fired on a centre-tap — Compose should toggle chrome visibility. */
    var onChromeTap: () -> Unit = {},
    /** Fired on a long-press — Compose can show copy-text menu. */
    var onLongPress: () -> Unit = {},
) : ReaderView(context) {

    init {
        setAdapter(PageAdapter(context, core))
        setLinksEnabled(true)
    }

    // ── Overrides ─────────────────────────────────────────────────────────────

    override fun onTapMainDocArea() {
        onChromeTap()
    }

    override fun onMoveToChild(i: Int) {
        super.onMoveToChild(i)
        onPageChanged(i)
    }

    override fun onMoveOffChild(i: Int) {
        // intentionally empty
    }

    override fun onLongPress(e: MotionEvent) {
        onLongPress()
    }

    // ── Scroll direction ──────────────────────────────────────────────────────

    /** Switch between horizontal (default) and vertical page scrolling. */
    fun setScrollHorizontal(horizontal: Boolean) {
        setHorizontalScrolling(horizontal)
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun search(text: String, direction: Int = +1, onFound: (page: Int) -> Unit = {}) {
        val task = object : SearchTask(context, core) {
            override fun onTextFound(result: SearchTaskResult) {
                SearchTaskResult.set(result)
                setDisplayedViewIndex(result.pageNumber)
                resetupChildren()
                onFound(result.pageNumber)
            }
        }
        task.go(text, direction, displayedViewIndex, -1)
    }

    fun clearSearch() {
        SearchTaskResult.set(null)
        resetupChildren()
    }

    /** Plain text for the current page — call from a background thread. */
    fun getCurrentPageText(): String = core.getPageText(displayedViewIndex)
}