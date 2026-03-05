package com.example.bookReader.ui.theme

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
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
            0.393f, 0.769f, 0.189f, 0f,   0f,
            0.349f, 0.686f, 0.168f, 0f,   0f,
            0.272f, 0.534f, 0.131f, 0f,   0f,
            0f,     0f,     0f,     1f,   0f
        ))

        // Darker, more muted sepia — better for low-light reading
        DARK_SEPIA -> ColorMatrix(floatArrayOf(
            0.20f, 0.40f, 0.10f, 0f,  10f,
            0.18f, 0.36f, 0.09f, 0f,   6f,
            0.14f, 0.28f, 0.07f, 0f,   2f,
            0f,    0f,    0f,    1f,   0f
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

// ─────────────────────────────────────────────────────────────────────────────
// Reader view
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Bridges MuPDF's View-based rendering into Compose-friendly callbacks.
 *
 * Responsibilities:
 *  • Page rendering + HQ zoom (via PageAdapter / ReaderView)
 *  • Chrome-toggle on centre-tap
 *  • Live colour-theme switching (hardware-layer ColorMatrix)
 *  • PDF link enable/disable toggle
 *  • Horizontal ↔ vertical scroll-direction toggle
 *  • In-document search (forward & backward)
 *  • Region-based text extraction for drag-to-select copy
 */
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

    // ── Theme ─────────────────────────────────────────────────────────────────

    /**
     * Apply a colour theme using a hardware-layer [ColorMatrix].
     * NORMAL removes any existing layer so there is zero rendering overhead.
     */
    fun applyTheme(theme: PdfTheme) {
        val matrix = theme.toColorMatrix()
        if (matrix == null) {
            setLayerType(View.LAYER_TYPE_NONE, null)
        } else {
            val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
            setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        }
        invalidate()
    }

    // ── Scroll direction ──────────────────────────────────────────────────────

    /** true = swipe left/right between pages; false = scroll up/down continuously. */
    fun setScrollHorizontal(horizontal: Boolean) = setHorizontalScrolling(horizontal)

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

    // ── Text extraction ───────────────────────────────────────────────────────

    /** Full plain-text of the currently visible page. Run on a background thread. */
    fun getCurrentPageText(): String = core.getPageText(displayedViewIndex)

    /**
     * Extract text that falls within a rectangle defined in **screen pixels**.
     *
     * The method converts the screen rect to PDF-point coordinates using the
     * current page view's layout transform, then delegates to
     * [MuPDFCore.getTextInPageRegion].  Call from a background thread.
     *
     * @param sx0  screen-x of the selection start (from long-press origin)
     * @param sy0  screen-y of the selection start
     * @param sx1  screen-x of the selection end (from drag position)
     * @param sy1  screen-y of the selection end
     */
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