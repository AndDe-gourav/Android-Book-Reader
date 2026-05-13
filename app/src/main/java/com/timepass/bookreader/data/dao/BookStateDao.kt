package com.timepass.bookreader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timepass.bookreader.data.entity.BookStateEntity
import com.timepass.bookreader.data.entity.ReadingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertState(state: BookStateEntity)

    @Query("SELECT * FROM book_state WHERE bookId = :bookId")
    suspend fun getState(bookId: Long): BookStateEntity?

    // NEW: Observe book state changes reactively
    @Query("SELECT * FROM book_state WHERE bookId = :bookId")
    fun observeState(bookId: Long): Flow<BookStateEntity?>

    @Query("""
        UPDATE book_state
        SET currentPage = :page, status = :status
        WHERE bookId = :bookId
    """)
    suspend fun updateProgress(
        bookId: Long,
        page: Int,
        status: ReadingStatus
    )

    @Query("UPDATE book_state SET isFavorite = :favorite WHERE bookId = :bookId")
    suspend fun setFavorite(bookId: Long, favorite: Boolean)
}