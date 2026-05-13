package com.timepass.bookreader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timepass.bookreader.data.entity.CollectionEntity
import com.timepass.bookreader.data.entity.CollectionWithBooks
import com.timepass.bookreader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    // 1. Define the Channel for UI Events
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // 2. Helper function to send snackbar messages
    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackbar(message))
        }
    }

    val allCollections: StateFlow<List<CollectionEntity>> = repository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCollectionsWithBooks: StateFlow<List<CollectionWithBooks>> =
        repository.getAllCollectionsWithBooks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _selectedCollection = MutableStateFlow<CollectionWithBooks?>(null)
    val selectedCollection: StateFlow<CollectionWithBooks?> = _selectedCollection.asStateFlow()

    fun createCollection(name: String) {
        viewModelScope.launch {
            try {
                repository.createCollection(name)
                showSnackbar("Collection '$name' created")
            } catch (e: Exception) {
                showSnackbar("Failed to create collection")
            }
        }
    }

    fun addBookToCollection(bookId: Long, collectionId: Long) {
        viewModelScope.launch {
            try {
                repository.addBookToCollection(bookId, collectionId)
                showSnackbar("Added to collection")
            } catch (e: Exception) {
                showSnackbar("Failed to add book")
            }
        }
    }

    fun removeBookFromCollection(bookId: Long, collectionId: Long) {
        viewModelScope.launch {
            try {
                repository.removeBookFromCollection(bookId, collectionId)
                showSnackbar("Removed from collection")
            } catch (e: Exception) {
                showSnackbar("Failed to remove book")
            }
        }
    }

    fun toggleBookInCollection(bookId: Long, collectionId: Long, currentlyIn: Boolean) {
        if (currentlyIn) {
            removeBookFromCollection(bookId, collectionId)
        } else {
            addBookToCollection(bookId, collectionId)
        }
    }

    fun loadCollection(collectionId: Long) {
        viewModelScope.launch {
            try {
                repository.getCollectionWithBooks(collectionId)
                    .collect { collectionWithBooks ->
                        _selectedCollection.value = collectionWithBooks
                    }
            } catch (e: Exception) {
                showSnackbar("Could not load collection")
            }
        }
    }

    fun clearSelectedCollection() {
        _selectedCollection.value = null
    }
}