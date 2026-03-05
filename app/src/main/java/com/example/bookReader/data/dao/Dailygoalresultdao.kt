package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookReader.data.entity.DailyGoalResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResult(result: DailyGoalResultEntity)

    @Query("SELECT * FROM daily_goal_results WHERE bookId = :bookId AND date = :date")
    suspend fun getResultForDay(bookId: Long, date: Long): DailyGoalResultEntity?

    @Query("""
        SELECT * FROM daily_goal_results
        WHERE bookId = :bookId
          AND date >= :from
          AND date < :to
        ORDER BY date ASC
    """)
    suspend fun getResultsInRange(bookId: Long, from: Long, to: Long): List<DailyGoalResultEntity>

    /** Total days the goal was ever completed for a book — used for the streak counter. */
    @Query("SELECT COUNT(*) FROM daily_goal_results WHERE bookId = :bookId AND isCompleted = 1")
    suspend fun countCompletedDays(bookId: Long): Int

    /**
     * Room re-emits this Flow whenever ANY row in daily_goal_results changes.
     * Used as an invalidation signal in StatsViewModel so the calendar and
     * progress bar update the instant a result is upserted after a session ends.
     */
    @Query("SELECT * FROM daily_goal_results ORDER BY date DESC")
    fun observeAll(): Flow<List<DailyGoalResultEntity>>

    /** Per-book reactive observer — useful for a dedicated history screen. */
    @Query("""
        SELECT * FROM daily_goal_results
        WHERE bookId = :bookId
        ORDER BY date DESC
    """)
    fun observeResultsForBook(bookId: Long): Flow<List<DailyGoalResultEntity>>

    @Query("DELETE FROM daily_goal_results WHERE bookId = :bookId")
    suspend fun deleteResultsForBook(bookId: Long)
}