package com.example.bookReader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookStateEntity
import com.example.bookReader.data.entity.ReadingStatus
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookStateViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    // Favorite books - automatically updates when favorites change
    val favoriteBooks: StateFlow<List<BookEntity>> = repository.getFavoriteBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Books to read - automatically updates when status changes
    val toReadBooks: StateFlow<List<BookEntity>> = repository.getBooksByStatus(ReadingStatus.TO_READ)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Currently reading books - automatically updates when status changes
    val readingBooks: StateFlow<List<BookEntity>> = repository.getBooksByStatus(ReadingStatus.READING)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Completed books - automatically updates when status changes
    val completedBooks: StateFlow<List<BookEntity>> = repository.getBooksByStatus(ReadingStatus.COMPLETED)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recent books (sorted by last opened) - automatically updates
    val recentBooks: StateFlow<List<BookEntity>> = repository.getRecentBooks(limit = 10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Get state for a specific book (one-time query)
     */
    suspend fun getBookState(bookId: Long): BookStateEntity? =
        repository.getBookState(bookId)

    /**
     * Observe book state changes reactively
     * Returns a Flow that emits whenever the book state changes
     * UI will automatically update when state changes
     */
    fun observeBookState(bookId: Long): Flow<BookStateEntity?> =
        repository.observeBookState(bookId)

    /**
     * Update book state (progress, status, favorite)
     * UI will automatically update via Flows
     */
    fun updateBookState(
        bookId: Long,
        currentPage: Int? = null,
        status: ReadingStatus? = null,
        isFavorite: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateBookState(
                    bookId = bookId,
                    currentPage = currentPage,
                    status = status,
                    isFavorite = isFavorite
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Get books by specific reading status
     * This returns a new StateFlow for dynamic status filtering
     */
    fun getBooksByStatus(status: ReadingStatus): StateFlow<List<BookEntity>> {
        return repository.getBooksByStatus(status)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}