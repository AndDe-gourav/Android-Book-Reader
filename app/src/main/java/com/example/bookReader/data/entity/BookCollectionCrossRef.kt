package com.example.bookReader.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "book_collection_cross_ref",
    primaryKeys = ["bookId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["collectionId"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId")]
)
data class BookCollectionCrossRef(
    val bookId: Long,
    val collectionId: Long
)
