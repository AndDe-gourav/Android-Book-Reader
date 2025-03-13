package com.example.epubreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MetadataExtractor(private val context: Context) {


    data class BookMetadata(
        val title: String = "",
        val author: String = "",
        val coverImage: String? = null,
        val pageCount: Int = 0
    )

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, title: String): String {
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9]"), "_")
        val file = File(context.filesDir, "cover_$safeTitle.png")

        if (!file.exists()) {
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return file.absolutePath
    }


    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "Unknown"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex("_display_name")
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }

        return fileName
    }


    suspend fun extractPdfMetadata(uri: Uri): BookMetadata = withContext(Dispatchers.IO) {
        val title = getFileNameFromUri(uri)
        val author = "Unknown"
        var coverImage: String? = null
        var pageCount = 0

        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                pageCount = pdfRenderer.pageCount

                if (pageCount > 0) {
                    pdfRenderer.openPage(0).use { page ->
                        val bitmap = Bitmap.createBitmap(
                            page.width,
                            page.height,
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        coverImage = saveBitmapToFile( context, bitmap, title)
                    }
                }

                pdfRenderer.close()
            }


        } catch (e: IOException) {
            e.printStackTrace()
        }

        return@withContext BookMetadata(
            title = title.substringBeforeLast(".pdf", title),
            author = author,
            coverImage = coverImage,
            pageCount = pageCount
        )
    }
}
