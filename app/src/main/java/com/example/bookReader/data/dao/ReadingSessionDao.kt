package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookReader.data.entity.ReadingSessionEntity

@Dao
interface ReadingSessionDao {

    @Insert
    suspend fun insertSession(session: ReadingSessionEntity)

    @Query("""
        SELECT SUM(endTime - startTime)
        FROM reading_sessions
        WHERE bookId = :bookId
    """)
    suspend fun getTotalReadingTime(bookId: Long): Long

    @Query("""
        SELECT *
        FROM reading_sessions
        WHERE startTime BETWEEN :from AND :to
    """)
    suspend fun getSessionsBetween(from: Long, to: Long): List<ReadingSessionEntity>
}
