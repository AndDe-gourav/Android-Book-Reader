package com.timepass.bookreader.ui.pdfviewer

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.MotionEvent
import com.artifex.mupdf.fitz.Page
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.PageAdapter
import com.artifex.mupdf.viewer.ReaderView
import com.artifex.mupdf.viewer.SearchTask
import com.artifex.mupdf.viewer.SearchTaskResult

// ─────────────────────────────────────────────────────────────────────────────
// Theme definition — kept here so MuPdfReaderView owns its application logic
// ─────────────────────────────────────────────────────────────────────────────

enum class PdfTheme(val label: String) {
    NORMAL("Normal"),
    SEPIA("Sepia"),
    DARK_SEPIA("Dark Sepia"),
    NIGHT("Night Mode");

    /** Returns the ColorMatrix for this theme, or null for NORMAL (no filter). */
    fun toColorMatrix(): ColorMatrix? = when (this) {
        NORMAL -> null

        // Classic warm sepia — white paper becomes parchment, black ink stays dark
        SEPIA -> ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, -15f,
            0f, 0f, 1f, 0f, -16f,
            0f, 0f, 0f, 1f, 0f
        ))

        // Darker, more muted sepia — better for low-light reading
        DARK_SEPIA -> ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, -67f,
            0f, 1f, 0f, 0f, -88f,
            0f, 0f, 1f, 0f, -128f,
            0f, 0f, 0f, 1f, 0f
        ))

        // Full colour-inversion — black bg, white/light text; easiest on eyes in the dark
        NIGHT -> ColorMatrix(floatArrayOf(
            -1f,  0f,  0f, 0f, 255f,
            0f, -1f,  0f, 0f, 255f,
            0f,  0f, -1f, 0f, 255f,
            0f,  0f,  0f, 1f,   0f
        ))
    }
}

class MuPdfReaderView(
    context: Context,
    val core: MuPDFCore,
    var onPageChanged: (page: Int) -> Unit = {},
    var onChromeTap: () -> Unit = {},
) : ReaderView(context) {

    init {
        setAdapter(PageAdapter(context, core))
        setLinksEnabled(true)
    }

    // ── ReaderView overrides ──────────────────────────────────────────────────

    override fun onTapMainDocArea() = onChromeTap()

    override fun onMoveToChild(i: Int) {
        super.onMoveToChild(i)
        onPageChanged(i)
    }

    override fun onMoveOffChild(i: Int) { /* intentionally empty */ }

    /** Long-press is handled by the Compose selection overlay, not here. */
    override fun onLongPress(e: MotionEvent) { /* no-op */ }

    fun applyTheme(theme: PdfTheme) {
        val matrix = theme.toColorMatrix()
        if (matrix == null) {
            setLayerType(LAYER_TYPE_NONE, null)
        } else {
            val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
            setLayerType(LAYER_TYPE_HARDWARE, paint)
        }
        invalidate()
    }

    fun annotate() {
    }

    // ── Scroll direction ──────────────────────────────────────────────────────

    /** true = swipe left/right between pages; false = scroll up/down continuously. */
    fun setScrollHorizontal(horizontal: Boolean) = setHorizontalScrolling(horizontal)

    // ── Search ────────────────────────────────────────────────────────────────

    private var activeSearchTask: SearchTask? = null

    fun search(text: String, direction: Int = +1, onFound: (page: Int) -> Unit = {}) {
        activeSearchTask?.stop()

        activeSearchTask = object : SearchTask(context, core) {
            override fun onTextFound(result: SearchTaskResult) {
                SearchTaskResult.set(result)
                post {
                    setDisplayedViewIndex(result.pageNumber)
                    resetupChildren()
                }
                onFound(result.pageNumber)
            }
        }.also { task ->
            task.go(text, direction, displayedViewIndex, displayedViewIndex)
        }
    }

    fun clearSearch() {
        activeSearchTask?.stop()
        activeSearchTask = null
        SearchTaskResult.set(null)
        resetupChildren()
    }

    fun page (page: Int) : Page {
        return core.getPage(page)
    }

    fun getCurrentPageText(): String = core.getPageText(displayedViewIndex)

    fun getTextInScreenRect(sx0: Float, sy0: Float, sx1: Float, sy1: Float): String {
        val t = getCurrentPageTransform() ?: return ""
        val viewLeft = t[0]; val viewTop  = t[1]
        val viewW    = t[2]; val viewH    = t[3]
        if (viewW <= 0f || viewH <= 0f) return ""

        val pageSize = core.getPageSize(displayedViewIndex)
        if (pageSize.x <= 0f || pageSize.y <= 0f) return ""

        val scaleX = viewW / pageSize.x
        val scaleY = viewH / pageSize.y

        // screen → PDF point
        val px0 = (sx0 - viewLeft) / scaleX
        val py0 = (sy0 - viewTop)  / scaleY
        val px1 = (sx1 - viewLeft) / scaleX
        val py1 = (sy1 - viewTop)  / scaleY

        return core.getTextInPageRegion(displayedViewIndex, px0, py0, px1, py1)
    }
}