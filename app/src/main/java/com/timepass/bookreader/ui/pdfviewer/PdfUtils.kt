package com.timepass.bookreader.ui.pdfviewer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfUtil {

    private const val COVER_IMAGES_DIR = "book_covers"

    // Final saved cover width
    private const val COVER_WIDTH = 500

    // Render at higher resolution for sharper quality
    private const val RENDER_MULTIPLIER = 2f

    private const val JPEG_QUALITY = 90

    suspend fun extractPdfMetadata(
        context: Context,
        uri: Uri
    ): PdfMetadata? = withContext(Dispatchers.IO) {

        try {

            // Copy PDF to temp file instead of readBytes()
            val tempFile = File.createTempFile(
                "temp_pdf",
                ".pdf",
                context.cacheDir
            )

            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val document = Document.openDocument(tempFile.absolutePath)

            val title = document.getMetaData(Document.META_INFO_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: getFileNameFromUri(context, uri)

            val author = document.getMetaData(Document.META_INFO_AUTHOR)
                ?.takeIf { it.isNotBlank() }

            val creator = document.getMetaData(Document.META_INFO_CREATOR)

            val format = document.getMetaData(Document.META_FORMAT)

            val pageCount = document.countPages()

            val coverPath = extractAndSaveCover(
                context = context,
                document = document,
                uri = uri
            )

            document.destroy()

            tempFile.delete()

            PdfMetadata(
                title = title,
                author = author,
                creator = creator,
                format = format,
                totalPages = pageCount,
                coverImagePath = coverPath
            )

        } catch (e: Exception) {
            e.printStackTrace()

            PdfMetadata(
                title = getFileNameFromUri(context, uri),
                author = null,
                creator = null,
                format = null,
                totalPages = 0,
                coverImagePath = null
            )
        }
    }

    private suspend fun extractAndSaveCover(
        context: Context,
        document: Document,
        uri: Uri
    ): String? = withContext(Dispatchers.IO) {

        try {

            if (document.countPages() == 0) {
                return@withContext null
            }

            val coversDir = File(context.filesDir, COVER_IMAGES_DIR)

            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }

            val fileName = "cover_${uri.hashCode()}.jpg"

            val outputFile = File(coversDir, fileName)

            // Return cached cover
            if (outputFile.exists()) {
                return@withContext outputFile.absolutePath
            }

            val page = document.loadPage(0)

            val bounds = page.bounds

            // Final display width
            val targetWidth = COVER_WIDTH

            // Render larger internally for sharper result
            val renderWidth = (targetWidth * RENDER_MULTIPLIER).toInt()

            val scale = renderWidth / bounds.x1

            val renderHeight = (bounds.y1 * scale).toInt()

            // High-resolution render bitmap
            val renderBitmap = Bitmap.createBitmap(
                renderWidth,
                renderHeight,
                Bitmap.Config.ARGB_8888
            )

            // White background
            renderBitmap.eraseColor(android.graphics.Color.WHITE)

            val matrix = Matrix(scale)

            val device = com.artifex.mupdf.fitz.android.AndroidDrawDevice(
                renderBitmap
            )

            page.run(device, matrix, null)

            device.close()
            device.destroy()

            page.destroy()

            // Final downscaled bitmap
            val finalHeight = (
                    renderBitmap.height *
                            (targetWidth.toFloat() / renderBitmap.width)
                    ).toInt()

            val finalBitmap = Bitmap.createScaledBitmap(
                renderBitmap,
                targetWidth,
                finalHeight,
                true
            )

            renderBitmap.recycle()

            // Save JPEG
            FileOutputStream(outputFile).use { out ->
                finalBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    JPEG_QUALITY,
                    out
                )
            }

            finalBitmap.recycle()

            outputFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get file name from URI
     */
    private fun getFileNameFromUri(
        context: Context,
        uri: Uri
    ): String {

        return try {

            context.contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )?.use { cursor ->

                val nameIndex =
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                cursor.moveToFirst()

                cursor.getString(nameIndex)

            } ?: uri.lastPathSegment ?: "Unknown"

        } catch (e: Exception) {

            e.printStackTrace()

            uri.lastPathSegment ?: "Unknown"
        }
    }
}

data class PdfMetadata(
    val title: String,
    val author: String?,
    val creator: String?,
    val format: String?,
    val totalPages: Int,
    val coverImagePath: String?
)