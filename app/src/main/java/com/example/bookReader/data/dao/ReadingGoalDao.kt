package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookReader.data.entity.ReadingGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoal(goal: ReadingGoalEntity)

    @Query("SELECT * FROM reading_goals WHERE bookId = :bookId")
    suspend fun getGoal(bookId: Long): ReadingGoalEntity?

    /**
     * Room re-emits this Flow whenever ANY row in reading_goals changes.
     * Used as an invalidation signal in StatsViewModel so the stats list
     * updates immediately when the user sets or changes a goal.
     */
    @Query("SELECT * FROM reading_goals")
    fun observeAll(): Flow<List<ReadingGoalEntity>>
}