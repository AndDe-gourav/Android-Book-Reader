package com.example.bookReader.ui

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

    /** Flat list of all collection entities (name + id). */
    val allCollections: StateFlow<List<CollectionEntity>> = repository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Every collection together with its member books.
     * Consumed by the Collection shelf view and the "add to collection" dialog
     * to know which collections already contain the selected book.
     */
    val allCollectionsWithBooks: StateFlow<List<CollectionWithBooks>> =
        repository.getAllCollectionsWithBooks()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _selectedCollection = MutableStateFlow<CollectionWithBooks?>(null)
    val selectedCollection: StateFlow<CollectionWithBooks?> = _selectedCollection.asStateFlow()

    /** Create a new collection (fire-and-forget from the UI). */
    fun createCollection(name: String) {
        viewModelScope.launch {
            try {
                repository.createCollection(name)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /** Add a book to a collection. */
    fun addBookToCollection(bookId: Long, collectionId: Long) {
        viewModelScope.launch {
            try {
                repository.addBookToCollection(bookId, collectionId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /** Remove a book from a collection. */
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
     * Toggle a book's membership in a collection.
     * If the book is already in the collection it is removed, otherwise it is added.
     */
    fun toggleBookInCollection(bookId: Long, collectionId: Long, currentlyIn: Boolean) {
        if (currentlyIn) {
            removeBookFromCollection(bookId, collectionId)
        } else {
            addBookToCollection(bookId, collectionId)
        }
    }

    /** Load a specific collection with its books (updates [selectedCollection] reactively). */
    fun loadCollection(collectionId: Long) {
        viewModelScope.launch {
            repository.getCollectionWithBooks(collectionId)
                .collect { collectionWithBooks ->
                    _selectedCollection.value = collectionWithBooks
                }
        }
    }

    fun clearSelectedCollection() {
        _selectedCollection.value = null
    }
}