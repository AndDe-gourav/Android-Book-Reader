package com.example.bookReader.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithState(
    @Embedded val book: BookEntity,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId"
    )
    val state: BookStateEntity
)