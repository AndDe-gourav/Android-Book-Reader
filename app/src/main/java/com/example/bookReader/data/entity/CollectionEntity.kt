package com.example.bookReader.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val collectionId: Long = 0,

    val name: String,

    val createdAt: Long = System.currentTimeMillis()
)
