package com.example.epubreader.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM book ORDER BY timestamp DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book WHERE id = :bookId")
    fun getBookById(bookId: Int): Flow<Book>

    @Query("SELECT * FROM book WHERE uri = :bookUri")
    fun getBookByUri(bookUri: String): Flow<Book?>

    @Query("SELECT * FROM book WHERE favourite = 1")
    fun getFavoriteBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book WHERE toRead = 1")
    fun getToReadBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book ORDER BY timestamp DESC LIMIT 1")
    fun lastOpenedBook(): Flow<Book>

    @Query("SELECT * FROM book WHERE collection = :collection")
    fun getBooksInCollection(collection: String): Flow<List<Book>>

    @Query("SELECT lastPage FROM book WHERE uri = :bookUri")
    fun getLastPage(bookUri: String): Flow<Int>

    @Query("SELECT totalPages FROM book WHERE uri = :bookUri")
    fun getTotalPage(bookUri: String): Flow<Int>

    @Query("SELECT * FROM book WHERE doneReading = 1")
    fun getCompletedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book WHERE collection IS NOT NULL AND collection != ''  ")
    fun getAllCollections(): Flow<List<Book>>

    @Insert
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE book SET favourite = :isFavorite WHERE uri = :bookUri")
    suspend fun updateFavoriteStatus(bookUri: String, isFavorite: Int)

    @Query("UPDATE book SET toRead = :isToRead WHERE uri = :bookUri")
    suspend fun updateToReadStatus(bookUri: String, isToRead: Int)

    @Query("UPDATE book SET collection = :collectionName WHERE uri = :bookUri")
    suspend fun updateCollection(bookUri: String, collectionName: String)

    @Query("UPDATE book SET doneReading = :isDoneReading WHERE uri = :bookUri")
    suspend fun updateDoneReadingStatus(bookUri: String, isDoneReading: Int)

    @Query("UPDATE book SET lastPage = :lastPage WHERE uri = :bookUri")
    suspend fun updateLastPage(bookUri: String, lastPage: Int)
}
