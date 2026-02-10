package com.example.bookReader.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.dao.BookCollectionDao
import com.example.bookReader.data.dao.CollectionDao
import com.example.bookReader.data.entity.CollectionEntity
import com.example.bookReader.data.entity.CollectionWithBooks
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
    private val collectionDao: CollectionDao,
    private val bookCollectionDao: BookCollectionDao
) : ViewModel() {

    // All collections
    val allCollections: StateFlow<List<CollectionEntity>> = collectionDao.getAllCollections()
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
            collectionDao.insertCollection(
                CollectionEntity(name = name)
            )
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
                bookCollectionDao.addBookToCollection(
                    com.example.bookReader.data.entity.BookCollectionCrossRef(
                        bookId = bookId,
                        collectionId = collectionId
                    )
                )
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
                bookCollectionDao.removeBookFromCollection(bookId, collectionId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Load a specific collection with its books
     */
    fun loadCollection(collectionId: Long) {
        viewModelScope.launch {
            collectionDao.getCollectionWithBooks(collectionId)
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