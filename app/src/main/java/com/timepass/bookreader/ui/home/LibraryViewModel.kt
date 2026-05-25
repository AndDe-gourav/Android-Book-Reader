package com.timepass.bookreader.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timepass.bookreader.UiEventManager
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.repository.BookRepository
import com.timepass.bookreader.ui.UiEvent
import com.timepass.bookreader.ui.pdfviewer.PdfUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BookShelfType {
    object Recent : BookShelfType()
    object Favorites : BookShelfType()
    object ToRead : BookShelfType()
    object Completed : BookShelfType()
    object Collection : BookShelfType()
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {


    init {
        restoreLastOpenedBook()
    }
    val allBooks: StateFlow<List<BookEntity>> = repository.getLibrary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    private val _currentBookShelf = MutableStateFlow<BookShelfType>(BookShelfType.Recent)
    val currentBookShelf: StateFlow<BookShelfType> = _currentBookShelf.asStateFlow()

    private val _pendingOpenBookId = MutableStateFlow<Long?>(null)
    val pendingOpenBookId = _pendingOpenBookId

    fun requestOpenBook(bookId: Long) {
        _pendingOpenBookId.value = bookId
    }

    fun clearPendingOpenBook() {
        _pendingOpenBookId.value = null
    }

    suspend fun importPdf( context: Context, uri: Uri ): Long {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
        val metadata = PdfUtil.extractPdfMetadata(context, uri)
        return metadata?.let { meta ->
            addBook(
                title = meta.title,
                author = meta.author,
                uri = uri,
                coverImagePath = meta.coverImagePath,
                totalPages = meta.totalPages,
                creator = meta.creator,
                format = meta.format
            )
        } ?: -1L
    }

    suspend fun addBook(
        title: String,
        author: String?,
        creator: String?,
        format: String?,
        uri: Uri,
        coverImagePath: String?,
        totalPages: Int
    ): Long {
        return try {
            val bookId = repository.addBook(
                title,
                author,
                format,
                creator,
                uri,
                coverImagePath,
                totalPages,
            )
            selectBookById(bookId)
            showSnackbar("Book added to library")
            bookId
        } catch (e: Exception) {
            showSnackbar("Failed to add book: ${e.message}")
            -1L
        }
    }

    fun selectBookById(bookId: Long) {
        viewModelScope.launch {
            val book = allBooks.value.find { it.bookId == bookId }
                ?: repository.getBookById(bookId)
            _selectedBook.value = book
        }
    }

    fun selectBook(book: BookEntity) {
        _selectedBook.value = book
    }

    fun changeBookShelf(shelfType: BookShelfType) {
        _currentBookShelf.value = shelfType
    }

    fun selectNextBook() {
        val books = allBooks.value
        val currentBook = _selectedBook.value ?: return

        val currentIndex = books.indexOfFirst {
            it.bookId == currentBook.bookId
        }

        if (currentIndex != -1 && currentIndex < books.lastIndex) {
            _selectedBook.value = books[currentIndex + 1]
        }
    }

    fun selectPreviousBook() {
        val books = allBooks.value
        val currentBook = _selectedBook.value ?: return

        val currentIndex = books.indexOfFirst {
            it.bookId == currentBook.bookId
        }

        if (currentIndex > 0) {
            _selectedBook.value = books[currentIndex - 1]
        }
    }
    fun updateBookTitle(bookId: Long, newTitle: String) {
        viewModelScope.launch {
            try {
                repository.updateBookTitle(bookId, newTitle)
                showSnackbar("Title updated")
            } catch (e: Exception) {
                showSnackbar("Failed to update title")
            }
        }
    }

    fun updateBookAuthor(bookId: Long, newAuthor: String) {
        viewModelScope.launch {
            try {
                repository.updateBookAuthor(bookId, newAuthor)
                showSnackbar("Author updated")
            } catch (e: Exception) {
                showSnackbar("Failed to update author")
            }
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteBook(bookId)
                restoreLastOpenedBook()
                showSnackbar("Book removed")
            } catch (e: Exception) {
                showSnackbar("Failed to delete book")
            }
        }
    }

    fun restoreLastOpenedBook() {
        viewModelScope.launch {
            val lastBook = repository.getLastOpenedBook()
            _selectedBook.value = lastBook
        }
    }

    fun updateLastOpened(bookId: Long) {
        viewModelScope.launch {
            repository.updateLastOpened(
                bookId,
                System.currentTimeMillis()
            )
        }
    }

    fun onFeedBackIconClicked(context: Context) {
        val deviceModel = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        val appVersion = "1.0.0"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf("gourav.and.de@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback: Book Reader App")
            putExtra(Intent.EXTRA_TEXT, """
            --- Device Info ---
            Model: $deviceModel
            OS Version: Android $androidVersion
            App Version: $appVersion
            
            --- Feedback ---
            Enter your feedback here:
            
        """.trimIndent())
        }

        context.startActivity(Intent.createChooser(intent, "Send Feedback via..."))
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            UiEventManager.sendEvent(
                UiEvent.ShowSnackbar(message)
            )
        }
    }
}