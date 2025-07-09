package com.example.epubreader.model.timeStorage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeGoalDao {

    @Query("SELECT * FROM TimeGoal WHERE uri = :bookUri")
    suspend fun getBookGoal(bookUri: String): TimeGoal?

    @Query("SELECT startTime FROM TimeGoal WHERE uri = :bookUri")
    suspend fun getStartingTime(bookUri: String):Long?

    @Query("SELECT totalTime FROM TimeGoal WHERE uri = :bookUri")
    suspend fun getTotalTime(bookUri: String):Long?

    @Insert
    suspend fun insertBook(book: TimeGoal):Long?

    @Query("UPDATE TimeGoal SET timeGoal = :time WHERE uri = :bookUri")
    suspend fun updateBookTimeGoal(bookUri: String, time: Int)

    @Query("SELECT * FROM TimeGoal WHERE uri = :bookUri")
    fun getBookByUri(bookUri: String): Flow<TimeGoal?>

    @Query("UPDATE TimeGoal SET startTime = :startTime WHERE uri = :bookUri")
    suspend fun updateStartTime(bookUri: String, startTime: Long)

    @Query("UPDATE TimeGoal SET totalTime = :totalTime WHERE uri = :bookUri")
    suspend fun updateTotalTime(bookUri: String, totalTime: Long)

    @Delete
    suspend fun deleteBook(book: TimeGoal)

}

