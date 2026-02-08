package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookReader.data.entity.ReadingGoalEntity

@Dao
interface ReadingGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoal(goal: ReadingGoalEntity)

    @Query("SELECT * FROM reading_goals WHERE bookId = :bookId")
    suspend fun getGoal(bookId: Long): ReadingGoalEntity?
}
