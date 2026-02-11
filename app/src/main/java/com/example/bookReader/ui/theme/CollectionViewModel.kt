package com.example.bookReader.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.entity.CollectionEntity
import com.example.bookReader.data.entity.CollectionWithBooks
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    // All collections - automatically updates when data changes
    val allCollections: StateFlow<List<CollectionEntity>> = repository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Currently selected collection with books
    private val _selectedCollection = MutableStateFlow<CollectionWithBooks?>(null)
    val selectedCollection: StateFlow<CollectionWithBooks?> = _selectedCollection.asStateFlow()

    /**
     * Create a new collection
     */
    suspend fun createCollection(name: String): Long {
        return try {
            repository.createCollection(name)
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Add a book to a collection
     */
    fun addBookToCollection(bookId: Long, collectionId: Long) {
        viewModelScope.launch {
            try {
                repository.addBookToCollection(bookId, collectionId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Remove a book from a collection
     */
    fun removeBookFromCollection(bookId: Long, collectionId: Long) {
        viewModelScope.launch {
            try {
                repository.removeBookFromCollection(bookId, collectionId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Load a specific collection with its books
     * This will automatically update when the collection changes
     */
    fun loadCollection(collectionId: Long) {
        viewModelScope.launch {
            repository.getCollectionWithBooks(collectionId)
                .collect { collectionWithBooks ->
                    _selectedCollection.value = collectionWithBooks
                }
        }
    }

    /**
     * Clear the selected collection
     */
    fun clearSelectedCollection() {
        _selectedCollection.value = null
    }
}