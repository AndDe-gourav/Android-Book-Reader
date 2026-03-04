package com.example.bookReader.ui.theme

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
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
    // All books from the library - automatically updates when books are added/removed
    val allBooks: StateFlow<List<BookEntity>> = repository.getLibrary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Currently selected book for reading
    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    // Current shelf being viewed
    private val _currentBookShelf = MutableStateFlow<BookShelfType>(BookShelfType.Recent)
    val currentBookShelf: StateFlow<BookShelfType> = _currentBookShelf.asStateFlow()

    // UI state for snackbar messages
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    /**
     * Add a new book to the library
     * UI will automatically update via allBooks Flow
     */
    suspend fun addBook(
        title: String,
        author: String?,
        uri: Uri,
        coverImagePath: String?,
        totalPages: Int
    ): Long {
        return try {
            val bookId = repository.addBook(title, author, uri, coverImagePath, totalPages)
            showSnackbar("Book added to library")
            bookId
        } catch (e: Exception) {
            showSnackbar("Failed to add book: ${e.message}")
            -1L
        }
    }

    /**
     * Select a book for reading
     */
    fun selectBook(book: BookEntity) {
        _selectedBook.value = book
    }


    /**
     * Clear the selected book
     */
    fun clearSelectedBook() {
        _selectedBook.value = null
    }

    /**
     * Change the current bookshelf view
     */
    fun changeBookShelf(shelfType: BookShelfType) {
        _currentBookShelf.value = shelfType
    }

    fun updateBookTitle(uri: Uri, newTitle: String) {
        viewModelScope.launch {
            try {
                repository.updateBookTitle(uri, newTitle)
                restoreLastOpenedBook()
                showSnackbar("Title updated")
            } catch (e: Exception) {
                showSnackbar("Failed to update title")
            }
        }
    }

    fun updateBookAuthor(uri: Uri, newAuthor: String) {
        viewModelScope.launch {
            try {
                repository.updateBookAuthor(uri, newAuthor)
                restoreLastOpenedBook()
                showSnackbar("Author updated")
            } catch (e: Exception) {
                showSnackbar("Failed to update author")
            }
        }
    }

    /**
     * Delete a book from library
     * UI will automatically update via allBooks Flow
     */
    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteBook(bookId)
                restoreLastOpenedBook()
                showSnackbar("Book deleted")
            } catch (e: Exception) {
                showSnackbar("Failed to delete book")
            }
        }
    }

    /**
     * Open PDF input stream
     */
    fun openPdf(bookId: Long): InputStream? {
        return try {
            repository.openPdf(bookId)
        } catch (e: Exception) {
            showSnackbar("Failed to open PDF: ${e.message}")
            null
        }
    }



    fun restoreLastOpenedBook() {
        viewModelScope.launch {
            Log.d("recent", "run")
            val lastBook = repository.getLastOpenedBook()
            _selectedBook.value = lastBook
        }
    }

    /**
     * Show snackbar message
     */
    private fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    /**
     * Clear snackbar message
     */
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}