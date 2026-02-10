package com.example.bookReader.ui.theme

import android.content.Context
import android.net.Uri
import com.artifex.mupdf.fitz.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class for handling PDF operations with MuPDF
 */
object PdfUtil {

    /**
     * Extract metadata from a PDF file
     */
    suspend fun extractPdfMetadata(context: Context, uri: Uri): PdfMetadata? = withContext(Dispatchers.IO) {
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

                document.destroy()

                PdfMetadata(
                    title = title,
                    author = author,
                    totalPages = pageCount
                )
            }
        } catch (e: Exception) {
            // If metadata extraction fails, return basic info
            PdfMetadata(
                title = getFileNameFromUri(context, uri),
                author = null,
                totalPages = 0
            )
        }
    }

    /**
     * Get total page count from PDF
     */
    suspend fun getPdfPageCount(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val document = Document.openDocument(bytes, "application/pdf")
                val count = document.countPages()
                document.destroy()
                count
            } ?: 0
        } catch (e: Exception) {
            0
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
            uri.lastPathSegment ?: "Unknown"
        }
    }

    /**
     * Validate if URI points to a valid PDF
     */
    suspend fun isValidPdf(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val document = Document.openDocument(bytes, "application/pdf")
                val isValid = document.countPages() > 0
                document.destroy()
                isValid
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class to hold PDF metadata
 */
data class PdfMetadata(
    val title: String,
    val author: String?,
    val totalPages: Int
)