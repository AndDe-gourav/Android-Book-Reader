package com.timepass.bookreader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.entity.ReadingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: BookEntity): Long

    @Query("SELECT * FROM books ORDER BY lastOpenedAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE bookId = :bookId LIMIT 1")
    suspend fun getBookById(bookId: Long): BookEntity?

    @Query("DELETE FROM books WHERE bookId = :bookId")
    suspend fun deleteBook(bookId: Long)

    @Transaction
    @Query("""
        SELECT * FROM books 
        WHERE bookId IN (
            SELECT bookId FROM book_state WHERE status = :status
        )
        ORDER BY addedAt DESC
    """)
    fun getBooksByStatus(status: ReadingStatus): Flow<List<BookEntity>>

    @Transaction
    @Query("""
        SELECT * FROM books 
        WHERE bookId IN (
            SELECT bookId FROM book_state WHERE isFavorite = 1
        )
        ORDER BY addedAt DESC
    """)
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentBooks(limit: Int): Flow<List<BookEntity>>

    @Query("""
    SELECT * FROM books
    ORDER BY lastOpenedAt DESC
    LIMIT 1
""")
    suspend fun getLastOpenedBook(): BookEntity?

    @Query("""
    UPDATE books
    SET lastOpenedAt = :time
    WHERE bookId = :bookId
""")
    suspend fun updateLastOpened(
        bookId: Long,
        time: Long
    )

    @Query("""
        UPDATE books
        SET title = :title
        WHERE bookId = :bookId
    """)
    suspend fun updateBookTitle(
        bookId: Long,
        title: String
    )

    @Query("""
        UPDATE books
        SET author = :author
        WHERE bookId = :bookId
    """)
    suspend fun updateBookAuthor(
        bookId: Long,
        author: String
    )
}