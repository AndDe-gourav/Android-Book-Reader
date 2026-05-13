package com.timepass.bookreader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.entity.BookStateEntity
import com.timepass.bookreader.data.entity.ReadingStatus
import com.timepass.bookreader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookStateViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    // 1. UI Event Channel
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // 2. Helper function
    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackbar(message))
        }
    }

    val favoriteBooks: StateFlow<List<BookEntity>> = repository.getFavoriteBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val toReadBooks: StateFlow<List<BookEntity>> = repository.getBooksByStatus(ReadingStatus.TO_READ)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val readingBooks: StateFlow<List<BookEntity>> = repository.getBooksByStatus(ReadingStatus.READING)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedBooks: StateFlow<List<BookEntity>> = repository.getBooksByStatus(ReadingStatus.COMPLETED)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentBooks: StateFlow<List<BookEntity>> = repository.getRecentBooks(limit = 10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun getBookState(bookId: Long): BookStateEntity? =
        repository.getBookState(bookId)

    fun observeBookState(bookId: Long): Flow<BookStateEntity?> =
        repository.observeBookState(bookId)

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

                when {
                    isFavorite == true -> showSnackbar("Added to Favorites")
                    isFavorite == false -> showSnackbar("Removed from Favorites")
                    status == ReadingStatus.COMPLETED -> showSnackbar("Book marked as Finished!")
                    status == ReadingStatus.TO_READ -> showSnackbar("Book marked as To Read")
                }
            } catch (e: Exception) {
                showSnackbar("Failed to update book state")
            }
        }
    }

    fun getBooksByStatus(status: ReadingStatus): StateFlow<List<BookEntity>> {
        return repository.getBooksByStatus(status)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}