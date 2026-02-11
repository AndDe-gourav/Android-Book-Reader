package com.example.bookReader.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.bookReader.data.dao.BookCollectionDao
import com.example.bookReader.data.dao.BookDao
import com.example.bookReader.data.dao.BookStateDao
import com.example.bookReader.data.dao.CollectionDao
import com.example.bookReader.data.dao.ReadingGoalDao
import com.example.bookReader.data.dao.ReadingSessionDao
import com.example.bookReader.data.entity.BookCollectionCrossRef
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookStateEntity
import com.example.bookReader.data.entity.CollectionEntity
import com.example.bookReader.data.entity.CollectionWithBooks
import com.example.bookReader.data.entity.ReadingGoalEntity
import com.example.bookReader.data.entity.ReadingSessionEntity
import com.example.bookReader.data.entity.ReadingStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val bookStateDao: BookStateDao,
    private val collectionDao: CollectionDao,
    private val bookCollectionDao: BookCollectionDao,
    private val sessionDao: ReadingSessionDao,
    private val goalDao: ReadingGoalDao,
    @ApplicationContext private val context: Context
) {

    // ==================== BOOK OPERATIONS ====================

    /**
     * Get all books as Flow for automatic UI updates
     */
    fun getLibrary(): Flow<List<BookEntity>> =
        bookDao.getAllBooks()

    /**
     * Add a new book to the library
     */
    suspend fun addBook(
        title: String,
        author: String?,
        uri: Uri,
        coverImagePath: String?,
        totalPages: Int
    ): Long {
        val bookId = bookDao.insertBook(
            BookEntity(
                title = title,
                author = author,
                uri = uri.toString(),
                totalPages = totalPages,
                coverImagePath = coverImagePath,
            )
        )

        // Initialize book state as TO_READ
        bookStateDao.insertState(
            BookStateEntity(
                bookId = bookId,
                status = ReadingStatus.TO_READ
            )
        )

        return bookId
    }

    /**
     * Get a specific book by ID
     */
    suspend fun getBookById(bookId: Long): BookEntity? =
        bookDao.getBookById(bookId)

    /**
     * Delete a book
     */
    suspend fun deleteBook(bookId: Long) {
        bookDao.deleteBook(bookId)
    }

    /**
     * Open PDF input stream for reading
     */
    fun openPdf(bookId: Long): InputStream {
        val book = runBlocking {
            bookDao.getBookById(bookId)
        } ?: throw IllegalStateException("Book not found")

        return context.contentResolver
            .openInputStream(book.uri.toUri())
            ?: throw FileNotFoundException()
    }

    // ==================== BOOK STATE OPERATIONS ====================

    /**
     * Get book state (one-time query)
     */
    suspend fun getBookState(bookId: Long): BookStateEntity? =
        bookStateDao.getState(bookId)

    /**
     * Observe book state changes reactively
     * UI will automatically update when state changes
     */
    fun observeBookState(bookId: Long): Flow<BookStateEntity?> =
        bookStateDao.observeState(bookId)

    /**
     * Update book progress
     */
    suspend fun updateProgress(bookId: Long, page: Int, totalPages: Int) {
        val status =
            if (page >= totalPages - 1) ReadingStatus.COMPLETED
            else ReadingStatus.READING

        bookStateDao.updateProgress(bookId, page, status)
    }

    /**
     * Update book state (progress, status, favorite)
     */
    suspend fun updateBookState(
        bookId: Long,
        currentPage: Int? = null,
        status: ReadingStatus? = null,
        isFavorite: Boolean? = null
    ) {
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
    }

    /**
     * Toggle favorite status for a book
     */
    suspend fun setFavorite(bookId: Long, favorite: Boolean) {
        bookStateDao.setFavorite(bookId, favorite)
    }

    /**
     * Get favorite books as reactive Flow
     * UI automatically updates when favorites change
     */
    fun getFavoriteBooks(): Flow<List<BookEntity>> =
        bookDao.getFavoriteBooks()

    /**
     * Get books by reading status as reactive Flow
     * UI automatically updates when status changes
     */
    fun getBooksByStatus(status: ReadingStatus): Flow<List<BookEntity>> =
        bookDao.getBooksByStatus(status)

    /**
     * Get recent books (sorted by last opened) as reactive Flow
     * UI automatically updates when books are opened
     */
    fun getRecentBooks(limit: Int = 10): Flow<List<BookEntity>> =
        bookDao.getRecentBooks(limit)

    // ==================== COLLECTION OPERATIONS ====================

    /**
     * Get all collections as Flow
     */
    fun getAllCollections(): Flow<List<CollectionEntity>> =
        collectionDao.getAllCollections()

    /**
     * Get collection with books as Flow
     */
    fun getCollectionWithBooks(collectionId: Long): Flow<CollectionWithBooks> =
        collectionDao.getCollectionWithBooks(collectionId)

    /**
     * Create a new collection
     */
    suspend fun createCollection(name: String): Long =
        collectionDao.insertCollection(CollectionEntity(name = name))

    /**
     * Add a book to a collection
     */
    suspend fun addBookToCollection(bookId: Long, collectionId: Long) {
        bookCollectionDao.addBookToCollection(
            BookCollectionCrossRef(bookId, collectionId)
        )
    }

    /**
     * Remove a book from a collection
     */
    suspend fun removeBookFromCollection(bookId: Long, collectionId: Long) {
        bookCollectionDao.removeBookFromCollection(bookId, collectionId)
    }

    // ==================== READING SESSION OPERATIONS ====================

    /**
     * Save a reading session
     */
    suspend fun saveSession(
        bookId: Long,
        startTime: Long,
        endTime: Long,
        startPage: Int,
        endPage: Int
    ) {
        sessionDao.insertSession(
            ReadingSessionEntity(
                bookId = bookId,
                startTime = startTime,
                endTime = endTime,
                startPage = startPage,
                endPage = endPage
            )
        )
    }

    /**
     * Get total reading time for a book
     */
    suspend fun getTotalReadingTime(bookId: Long): Long =
        sessionDao.getTotalReadingTime(bookId)

    /**
     * Get reading sessions between time range
     */
    suspend fun getSessionsBetween(from: Long, to: Long): List<ReadingSessionEntity> =
        sessionDao.getSessionsBetween(from, to)

    // ==================== READING GOAL OPERATIONS ====================

    /**
     * Set reading goal for a book
     */
    suspend fun setReadingGoal(bookId: Long, dailyMinutes: Int) {
        goalDao.setGoal(
            ReadingGoalEntity(
                bookId = bookId,
                dailyMinutesGoal = dailyMinutes
            )
        )
    }

    /**
     * Get reading goal for a book
     */
    suspend fun getReadingGoal(bookId: Long): ReadingGoalEntity? =
        goalDao.getGoal(bookId)
}