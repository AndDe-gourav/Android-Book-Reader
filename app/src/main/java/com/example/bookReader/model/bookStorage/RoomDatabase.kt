package com.example.bookReader.model.bookStorage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val author: String,
    val uri: String,
    val bookCover: String?,
    val favourite: Int,
    val toRead: Int,
    val collection: String,
    val doneReading: Int,
    val lastPage: Int,
    val totalPages: Int,
    val timestamp: Long
)
