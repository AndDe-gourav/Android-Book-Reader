package com.example.bookReader.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.bookReader.data.dao.BookCollectionDao
import com.example.bookReader.data.dao.BookDao
import com.example.bookReader.data.dao.BookStateDao
import com.example.bookReader.data.dao.CollectionDao
import com.example.bookReader.data.dao.DailyGoalResultDao
import com.example.bookReader.data.dao.ReadingGoalDao
import com.example.bookReader.data.dao.ReadingSessionDao
import com.example.bookReader.data.entity.BookCollectionCrossRef
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookStateEntity
import com.example.bookReader.data.entity.CollectionEntity
import com.example.bookReader.data.entity.CollectionWithBooks
import com.example.bookReader.data.entity.DailyGoalResultEntity
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
    private val dailyGoalResultDao: DailyGoalResultDao,
    @ApplicationContext private val context: Context
) {

    // ==================== BOOK OPERATIONS ====================

    fun getLibrary(): Flow<List<BookEntity>> = bookDao.getAllBooks()

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
        bookStateDao.insertState(BookStateEntity(bookId = bookId, status = ReadingStatus.TO_READ))
        return bookId
    }

    suspend fun getBookById(bookId: Long): BookEntity? = bookDao.getBookById(bookId)

    suspend fun deleteBook(bookId: Long) = bookDao.deleteBook(bookId)

    fun openPdf(bookId: Long): InputStream {
        val book = runBlocking { bookDao.getBookById(bookId) }
            ?: throw IllegalStateException("Book not found")
        return context.contentResolver.openInputStream(book.uri.toUri())
            ?: throw FileNotFoundException()
    }

    suspend fun getLastOpenedBook(): BookEntity? = bookDao.getLastOpenedBook()
    suspend fun updateBookTitle(bookId: Long, title: String) = bookDao.updateBookTitle(bookId, title)
    suspend fun updateBookAuthor(bookId: Long, author: String) = bookDao.updateBookAuthor(bookId, author)

    // ==================== BOOK STATE OPERATIONS ====================

    suspend fun getBookState(bookId: Long): BookStateEntity? = bookStateDao.getState(bookId)
    fun observeBookState(bookId: Long): Flow<BookStateEntity?> = bookStateDao.observeState(bookId)

    suspend fun updateProgress(bookId: Long, page: Int, totalPages: Int) {
        val status = if (page >= totalPages - 1) ReadingStatus.COMPLETED else ReadingStatus.READING
        bookStateDao.updateProgress(bookId, page, status)
    }

    suspend fun updateBookState(
        bookId: Long,
        currentPage: Int? = null,
        status: ReadingStatus? = null,
        isFavorite: Boolean? = null
    ) {
        val existingState = bookStateDao.getState(bookId)
        if (existingState != null) {
            if (currentPage != null || status != null) {
                bookStateDao.updateProgress(
                    bookId = bookId,
                    page = currentPage ?: existingState.currentPage,
                    status = status ?: existingState.status
                )
            }
            if (isFavorite != null) bookStateDao.setFavorite(bookId, isFavorite)
        } else {
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

    fun getFavoriteBooks(): Flow<List<BookEntity>> = bookDao.getFavoriteBooks()
    fun getBooksByStatus(status: ReadingStatus): Flow<List<BookEntity>> = bookDao.getBooksByStatus(status)
    fun getRecentBooks(limit: Int = 10): Flow<List<BookEntity>> = bookDao.getRecentBooks(limit)

    // ==================== COLLECTION OPERATIONS ====================

    fun getAllCollections(): Flow<List<CollectionEntity>> = collectionDao.getAllCollections()
    fun getCollectionWithBooks(collectionId: Long): Flow<CollectionWithBooks> = collectionDao.getCollectionWithBooks(collectionId)
    fun getAllCollectionsWithBooks(): Flow<List<CollectionWithBooks>> = collectionDao.getAllCollectionsWithBooks()
    suspend fun createCollection(name: String): Long = collectionDao.insertCollection(CollectionEntity(name = name))
    suspend fun addBookToCollection(bookId: Long, collectionId: Long) = bookCollectionDao.addBookToCollection(BookCollectionCrossRef(bookId, collectionId))
    suspend fun removeBookFromCollection(bookId: Long, collectionId: Long) = bookCollectionDao.removeBookFromCollection(bookId, collectionId)

    // ==================== READING SESSION OPERATIONS ====================

    suspend fun saveSession(
        bookId: Long, startTime: Long, endTime: Long, startPage: Int, endPage: Int
    ) {
        sessionDao.insertSession(
            ReadingSessionEntity(
                bookId = bookId, startTime = startTime, endTime = endTime,
                startPage = startPage, endPage = endPage
            )
        )
    }

    suspend fun getTotalReadingTime(bookId: Long): Long = sessionDao.getTotalReadingTime(bookId)
    suspend fun getReadingTimeBetween(bookId: Long, from: Long, to: Long): Long = sessionDao.getReadingTimeBetween(bookId, from, to)
    suspend fun getSessionsBetween(from: Long, to: Long): List<ReadingSessionEntity> = sessionDao.getSessionsBetween(from, to)

    /**
     * Emits whenever any reading session is inserted or modified.
     * StatsViewModel collects this to know when to recompute stats.
     */
    fun observeSessionChanges(): Flow<List<ReadingSessionEntity>> = sessionDao.observeAll()

    // ==================== READING GOAL OPERATIONS ====================

    suspend fun setReadingGoal(bookId: Long, dailyMinutes: Int) {
        goalDao.setGoal(ReadingGoalEntity(bookId = bookId, dailyMinutesGoal = dailyMinutes))
    }

    suspend fun getReadingGoal(bookId: Long): ReadingGoalEntity? = goalDao.getGoal(bookId)

    /**
     * Emits whenever any reading goal is set or changed.
     * StatsViewModel collects this to show a newly-created goal card immediately.
     */
    fun observeGoalChanges(): Flow<List<ReadingGoalEntity>> = goalDao.observeAll()

    // ==================== DAILY GOAL RESULT OPERATIONS ====================

    suspend fun saveDailyGoalResult(
        bookId: Long, dayStartMs: Long, goalMinutes: Int, minutesRead: Long
    ) {
        dailyGoalResultDao.upsertResult(
            DailyGoalResultEntity(
                bookId = bookId,
                date = dayStartMs,
                goalMinutes = goalMinutes,
                minutesRead = minutesRead,
                isCompleted = minutesRead >= goalMinutes
            )
        )
    }

    suspend fun getDailyGoalResultsInRange(bookId: Long, from: Long, to: Long): List<DailyGoalResultEntity> =
        dailyGoalResultDao.getResultsInRange(bookId, from, to)

    suspend fun countCompletedDays(bookId: Long): Int = dailyGoalResultDao.countCompletedDays(bookId)

    /**
     * Emits whenever any daily goal result is upserted (i.e. after a session ends or
     * at midnight). StatsViewModel uses this to refresh the progress bar and calendar.
     */
    fun observeDailyResultChanges(): Flow<List<DailyGoalResultEntity>> = dailyGoalResultDao.observeAll()
}