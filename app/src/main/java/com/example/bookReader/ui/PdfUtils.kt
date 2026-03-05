package com.example.bookReader.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.Matrix
import com.artifex.mupdf.fitz.android.AndroidDrawDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for handling PDF operations with MuPDF
 */
object PdfUtil {

    private const val COVER_IMAGES_DIR = "book_covers"

    /**
     * Extract metadata and cover from a PDF file
     */
    suspend fun extractPdfMetadata(context: Context, uri: Uri): PdfMetadata? =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    // Read the PDF into a byte array
                    val bytes = inputStream.readBytes()
                    // Open document from byte array
                    val document = Document.openDocument(bytes, "application/pdf")

                    val title = document.getMetaData(Document.META_INFO_TITLE)
                        ?.takeIf { it.isNotBlank() }
                        ?: getFileNameFromUri(context, uri)

                    val author = document.getMetaData(Document.META_INFO_AUTHOR)
                        ?.takeIf { it.isNotBlank() }

                    val pageCount = document.countPages()

                    // Extract and save cover image
                    val coverPath = extractAndSaveCover(context, document, uri)

                    document.destroy()

                    PdfMetadata(
                        title = title,
                        author = author,
                        totalPages = pageCount,
                        coverImagePath = coverPath
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If metadata extraction fails, return basic info
                PdfMetadata(
                    title = getFileNameFromUri(context, uri),
                    author = null,
                    totalPages = 0,
                    coverImagePath = null
                )
            }
        }

    /**
     * Extract cover and save to internal storage
     */
    /**
     * Extract cover and save to internal storage
     */
    private suspend fun extractAndSaveCover(
        context: Context,
        document: Document,
        uri: Uri,
        width: Int = 300
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (document.countPages() == 0) return@withContext null

            val page = document.loadPage(0)
            val bounds = page.bounds

            // Calculate scale to fit desired width while maintaining aspect ratio
            val scale = width / bounds.x1
            val height = (bounds.y1 * scale).toInt()

            // Create bitmap with white background
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE) // Fill white BEFORE AndroidDrawDevice

            // Create matrix for scaling
            val matrix = Matrix(scale)

            // Render page to bitmap using AndroidDrawDevice
            val device = AndroidDrawDevice(bitmap)
            page.run(device, matrix, null)
            device.close()
            device.destroy()
            page.destroy()

            // Save bitmap to file
            val savedPath = saveCoverToFile(context, bitmap, uri)
            bitmap.recycle() // Free memory

            savedPath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save cover bitmap to internal storage
     */
    private fun saveCoverToFile(context: Context, bitmap: Bitmap, uri: Uri): String? {
        return try {
            // Create covers directory if it doesn't exist
            val coversDir = File(context.filesDir, COVER_IMAGES_DIR)
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }

            // Create unique filename based on URI
            val fileName = "cover_${uri.hashCode()}.jpg"
            val file = File(coversDir, fileName)

            // Save bitmap as JPEG
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * Get file name from URI
     */
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: uri.lastPathSegment ?: "Unknown"
        } catch (e: Exception) {
            e.printStackTrace()
            uri.lastPathSegment ?: "Unknown"
        }
    }
}
/**
 * Data class to hold PDF metadata
 */
data class PdfMetadata(
    val title: String,
    val author: String?,
    val totalPages: Int,
    val coverImagePath: String?
)