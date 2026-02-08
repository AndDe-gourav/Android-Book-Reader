package com.example.bookReader.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithBooks(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "collectionId",
        entityColumn = "bookId",
        associateBy = Junction(BookCollectionCrossRef::class)
    )
    val books: List<BookEntity>
)