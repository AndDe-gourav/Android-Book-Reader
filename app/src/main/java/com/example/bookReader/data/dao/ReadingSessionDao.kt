package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookReader.data.entity.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Insert
    suspend fun insertSession(session: ReadingSessionEntity)

    @Query("""
        SELECT COALESCE(SUM(endTime - startTime), 0)
        FROM reading_sessions
        WHERE bookId = :bookId
    """)
    suspend fun getTotalReadingTime(bookId: Long): Long

    @Query("""
        SELECT COALESCE(SUM(endTime - startTime), 0)
        FROM reading_sessions
        WHERE bookId = :bookId
          AND startTime >= :from
          AND startTime < :to
    """)
    suspend fun getReadingTimeBetween(bookId: Long, from: Long, to: Long): Long

    @Query("""
        SELECT *
        FROM reading_sessions
        WHERE startTime BETWEEN :from AND :to
    """)
    suspend fun getSessionsBetween(from: Long, to: Long): List<ReadingSessionEntity>

    /**
     * Room re-emits this Flow whenever ANY row in reading_sessions is inserted,
     * updated, or deleted. Used as an invalidation signal in StatsViewModel so
     * the stats card refreshes in real time the moment a session is saved.
     */
    @Query("SELECT * FROM reading_sessions ORDER BY startTime DESC")
    fun observeAll(): Flow<List<ReadingSessionEntity>>
}