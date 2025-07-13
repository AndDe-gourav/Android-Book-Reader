package com.example.epubreader.model.timeStorage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeGoalDao {

    @Query("SELECT * FROM TimeGoal WHERE uri = :bookUri AND date = :date")
    suspend fun getBookGoal(bookUri: String, date: String): TimeGoal?

    @Query("SELECT totalTime FROM TimeGoal WHERE uri = :bookUri AND date = :date")
    suspend fun getTotalTime(bookUri: String, date: String):Long?

    @Query("SELECT goalCompleted FROM TimeGoal WHERE uri = :bookUri AND date = :date")
    suspend fun getGoalCompleted(bookUri: String, date: String):Int?

    @Query("SELECT * FROM TimeGoal WHERE timeGoal > 0 AND date = :date ")
    fun getTimeGoalBooks(date: String): Flow<List<TimeGoal>>
    @Insert
    suspend fun insertBook(book: TimeGoal):Long?

    @Query("UPDATE TimeGoal SET timeGoal = :time WHERE uri = :bookUri AND date = :date")
    suspend fun updateBookTimeGoal(bookUri: String, time: Int, date: String)

    @Query("SELECT * FROM TimeGoal WHERE uri = :bookUri AND date = :date")
    fun getBookByUri(bookUri: String, date: String): Flow<TimeGoal?>

    @Query("SELECT * FROM TimeGoal WHERE uri = :uri AND date = :date")
    fun getBookByUriAndDate(uri: String, date: String): Flow<TimeGoal?>

    @Query("UPDATE TimeGoal SET totalTime = :totalTime WHERE uri = :bookUri AND date = :date")
    suspend fun updateTotalTime(bookUri: String, totalTime: Long, date: String)

    @Query("UPDATE TimeGoal SET goalCompleted = :goalCompleted WHERE uri = :bookUri AND date = :date")
    suspend fun updateGoalCompleted(bookUri: String, goalCompleted: Int, date: String)

    @Delete
    suspend fun deleteBook(book: TimeGoal)

}

