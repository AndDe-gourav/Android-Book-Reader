package com.example.bookReader.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,

    val bookId: Long,

    val startTime: Long,
    val endTime: Long,

    val startPage: Int,
    val endPage: Int
)
