package com.example.bookReader.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [Index(value = ["uri"], unique = true)]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val bookId: Long = 0,

    val title: String,
    val author: String?,

    val uri: String,

    val totalPages: Int,
    val coverImagePath: String?,
    val addedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long? = null
)