package com.example.bookReader.model.timeStorage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TimeGoal")
data class TimeGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uri: String,
    val date: Int,
    val totalTime: Long,
    val timeGoal: Int,
    val goalCompleted: Int,
)
