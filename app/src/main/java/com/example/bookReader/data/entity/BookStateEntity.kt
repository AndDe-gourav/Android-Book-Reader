package com.example.bookReader.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "book_state",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookStateEntity(
    @PrimaryKey
    val bookId: Long,

    val status: ReadingStatus,

    val currentPage: Int = 0,

    val isFavorite: Boolean = false
)

enum class ReadingStatus {
    TO_READ,
    READING,
    COMPLETED
}
