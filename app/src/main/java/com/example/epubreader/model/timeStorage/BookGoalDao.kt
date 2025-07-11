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

    @Query("SELECT totalTime FROM TimeGoal WHERE uri = :bookUri")
    suspend fun getTotalTime(bookUri: String):Long?

    @Query("SELECT goalCompleted FROM TimeGoal WHERE uri = :bookUri")
    suspend fun getGoalCompleted(bookUri: String):Int?

    @Query("SELECT * FROM TimeGoal WHERE timeGoal > 0")
    fun getTimeGoalBooks(): Flow<List<TimeGoal>>
    @Insert
    suspend fun insertBook(book: TimeGoal):Long?

    @Query("UPDATE TimeGoal SET timeGoal = :time WHERE uri = :bookUri")
    suspend fun updateBookTimeGoal(bookUri: String, time: Int)

    @Query("SELECT * FROM TimeGoal WHERE uri = :bookUri")
    fun getBookByUri(bookUri: String): Flow<TimeGoal?>

    @Query("UPDATE TimeGoal SET totalTime = :totalTime WHERE uri = :bookUri")
    suspend fun updateTotalTime(bookUri: String, totalTime: Long)

    @Query("UPDATE TimeGoal SET goalCompleted = :goalCompleted WHERE uri = :bookUri")
    suspend fun updateGoalCompleted(bookUri: String, goalCompleted: Int)

    @Delete
    suspend fun deleteBook(book: TimeGoal)

}

