package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookWithState
import com.example.bookReader.data.entity.ReadingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: BookEntity): Long

    @Query("SELECT * FROM books ORDER BY addedAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE bookId = :bookId")
    suspend fun getBookById(bookId: Long): BookEntity?

    @Query("DELETE FROM books WHERE bookId = :bookId")
    suspend fun deleteBook(bookId: Long)

    // NEW: Get all books with their states as Flow
    @Transaction
    @Query("SELECT * FROM books ORDER BY addedAt DESC")
    fun getAllBooksWithState(): Flow<List<BookWithState>>

    // NEW: Get books by status with reactive Flow
    @Transaction
    @Query("""
        SELECT * FROM books 
        WHERE bookId IN (
            SELECT bookId FROM book_state WHERE status = :status
        )
        ORDER BY addedAt DESC
    """)
    fun getBooksByStatus(status: ReadingStatus): Flow<List<BookEntity>>

    // NEW: Get favorite books with reactive Flow
    @Transaction
    @Query("""
        SELECT * FROM books 
        WHERE bookId IN (
            SELECT bookId FROM book_state WHERE isFavorite = 1
        )
        ORDER BY addedAt DESC
    """)
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    // NEW: Get recent books with reactive Flow
    @Query("SELECT * FROM books ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentBooks(limit: Int): Flow<List<BookEntity>>
}