package com.example.bookReader.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * One row per (book, calendar-day).
 *
 * Written (upserted) whenever a reading session ends or midnight crosses while
 * the reader is open. The composite primary key (bookId + date) guarantees
 * there is always exactly one record per book per day, and re-opening the book
 * later that same day simply overwrites it with the latest cumulative total.
 *
 * [date] is always the start-of-day timestamp in the device's local timezone
 * (i.e. midnight of that day), so calendar lookups can match exactly on it.
 */
@Entity(
    tableName = "daily_goal_results",
    primaryKeys = ["bookId", "date"],
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
data class DailyGoalResultEntity(
    val bookId: Long,

    /** Midnight (00:00:00.000) of the day this record represents, in epoch-ms. */
    val date: Long,

    /** The daily goal that was active on this day (minutes). */
    val goalMinutes: Int,

    /** Actual reading time accumulated on this day (minutes). */
    val minutesRead: Long,

    /** True when minutesRead >= goalMinutes at the time of the last save. */
    val isCompleted: Boolean,

    /** Wall-clock time of the last upsert — useful for debugging. */
    val lastUpdatedAt: Long = System.currentTimeMillis()
)