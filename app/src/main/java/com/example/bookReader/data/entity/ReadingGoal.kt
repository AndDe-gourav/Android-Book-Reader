package com.example.bookReader.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_goals",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReadingGoalEntity(
    @PrimaryKey
    val bookId: Long,

    val dailyMinutesGoal: Int
)
