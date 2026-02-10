package com.example.bookReader.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.dao.BookDao
import com.example.bookReader.data.dao.BookStateDao
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookStateEntity
import com.example.bookReader.data.entity.ReadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookWithStateInfo(
    val book: BookEntity,
    val state: BookStateEntity?
)

@HiltViewModel
class BookStateViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val bookStateDao: BookStateDao
) : ViewModel() {

    // Combine books with their states
    private val booksWithState: StateFlow<List<BookWithStateInfo>> = bookDao.getAllBooks()
        .combine(MutableStateFlow(Unit)) { books, _ ->
            books.map { book ->
                val state = try {
                    bookStateDao.getState(book.bookId)
                } catch (e: Exception) {
                    null
                }
                BookWithStateInfo(book, state)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get books by reading status
    fun getBooksByStatus(status: ReadingStatus): StateFlow<List<BookEntity>> {
        return booksWithState
            .combine(MutableStateFlow(status)) { booksWithState, targetStatus ->
                booksWithState
                    .filter { it.state?.status == targetStatus }
                    .map { it.book }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // Get favorite books
    val favoriteBooks: StateFlow<List<BookEntity>> = booksWithState
        .combine(MutableStateFlow(Unit)) { booksWithState, _ ->
            booksWithState
                .filter { it.state?.isFavorite == true }
                .map { it.book }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get books to read
    val toReadBooks: StateFlow<List<BookEntity>> = booksWithState
        .combine(MutableStateFlow(Unit)) { booksWithState, _ ->
            booksWithState
                .filter { it.state?.status == ReadingStatus.TO_READ }
                .map { it.book }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get currently reading books
    val readingBooks: StateFlow<List<BookEntity>> = booksWithState
        .combine(MutableStateFlow(Unit)) { booksWithState, _ ->
            booksWithState
                .filter { it.state?.status == ReadingStatus.READING }
                .map { it.book }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get completed books
    val completedBooks: StateFlow<List<BookEntity>> = booksWithState
        .combine(MutableStateFlow(Unit)) { booksWithState, _ ->
            booksWithState
                .filter { it.state?.status == ReadingStatus.COMPLETED }
                .map { it.book }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get recent books (sorted by last opened)
    val recentBooks: StateFlow<List<BookEntity>> = booksWithState
        .combine(MutableStateFlow(Unit)) { booksWithState, _ ->
            booksWithState
                .map { it.book }
                .sortedByDescending { it.lastOpenedAt ?: 0 }
                .take(10) // Limit to 10 most recent
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Get state for a specific book
     */
    suspend fun getBookState(bookId: Long): BookStateEntity? {
        return try {
            bookStateDao.getState(bookId)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update book state
     */
    fun updateBookState(
        bookId: Long,
        currentPage: Int? = null,
        status: ReadingStatus? = null,
        isFavorite: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                val existingState = bookStateDao.getState(bookId)

                if (existingState != null) {
                    // Update existing state
                    if (currentPage != null || status != null) {
                        bookStateDao.updateProgress(
                            bookId = bookId,
                            page = currentPage ?: existingState.currentPage,
                            status = status ?: existingState.status
                        )
                    }
                    if (isFavorite != null) {
                        bookStateDao.setFavorite(bookId, isFavorite)
                    }
                } else {
                    // Create new state
                    bookStateDao.insertState(
                        BookStateEntity(
                            bookId = bookId,
                            status = status ?: ReadingStatus.TO_READ,
                            currentPage = currentPage ?: 0,
                            isFavorite = isFavorite ?: false
                        )
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}