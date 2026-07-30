package com.timepass.bookreader.ui.pdfviewer

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.SystemClock
import android.view.MotionEvent
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.PageAdapter
import com.artifex.mupdf.viewer.ReaderView
import com.artifex.mupdf.viewer.SearchTask
import com.artifex.mupdf.viewer.SearchTaskResult

enum class PdfTheme(val label: String) {
    NORMAL("Normal"),
    SEPIA("Sepia"),
    DARK_SEPIA("Dark Sepia"),
    NIGHT("Night Mode");

    fun toColorMatrix(): ColorMatrix? = when (this) {
        NORMAL -> null

        SEPIA -> ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, -15f,
            0f, 0f, 1f, 0f, -16f,
            0f, 0f, 0f, 1f, 0f
        ))

        DARK_SEPIA -> ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, -67f,
            0f, 1f, 0f, 0f, -88f,
            0f, 0f, 1f, 0f, -128f,
            0f, 0f, 0f, 1f, 0f
        ))

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
    var onPanStateChanged: (fraction: Float?) -> Unit = {},
) : ReaderView(context) {

    init {
        setAdapter(PageAdapter(context, core))
        setLinksEnabled(true)
    }

    override fun onTapMainDocArea() = onChromeTap()

    override fun onMoveToChild(i: Int) {
        super.onMoveToChild(i)
        onPageChanged(i)
    }

    override fun onMoveOffChild(i: Int) { /* intentionally empty */ }

    override fun onLongPress(e: MotionEvent) { /* no-op */ }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        reportPanState()
    }

    private fun reportPanState() {
        val transform = getCurrentPageTransform()
        if (transform == null || transform.size < 3) {
            onPanStateChanged(null)
            return
        }
        val viewLeft = transform[0]
        val viewWidth = transform[2]
        val overflow = viewWidth - width
        if (overflow <= 1f) {
            onPanStateChanged(null)
            return
        }
        onPanStateChanged((-viewLeft / overflow).coerceIn(0f, 1f))
    }

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

    fun scrollBy(dxPixels: Float, dyPixels: Float = 0f) {
        val now = SystemClock.uptimeMillis()
        val e1 = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        val e2 = MotionEvent.obtain(now, now, MotionEvent.ACTION_MOVE, 0f, 0f, 0)
        try {
            onScroll(e1, e2, dxPixels, dyPixels)
        } finally {
            e1.recycle()
            e2.recycle()
        }
    }

    fun panToFraction(fraction: Float) {
        val transform = getCurrentPageTransform() ?: return
        val viewLeft = transform[0]
        val viewWidth = transform[2]
        val overflow = viewWidth - width
        if (overflow <= 1f) return

        val targetLeft = -(fraction.coerceIn(0f, 1f) * overflow)
        val dx = viewLeft - targetLeft
        if (kotlin.math.abs(dx) >= 1f) scrollBy(dx)
    }

    fun settle() {
        val view = getDisplayedView() ?: return
        onUnsettle(view)
        onSettle(view)
    }

    fun setScrollHorizontal(horizontal: Boolean) = setHorizontalScrolling(horizontal)

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
}