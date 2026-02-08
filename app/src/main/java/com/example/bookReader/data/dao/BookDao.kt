package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookReader.data.entity.BookEntity
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
}
