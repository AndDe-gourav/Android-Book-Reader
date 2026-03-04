package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.bookReader.data.entity.CollectionEntity
import com.example.bookReader.data.entity.CollectionWithBooks
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Insert
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Query("SELECT * FROM collections")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Transaction
    @Query("SELECT * FROM collections WHERE collectionId = :id")
    fun getCollectionWithBooks(id: Long): Flow<CollectionWithBooks>

    @Transaction
    @Query("SELECT * FROM collections")
    fun getAllCollectionsWithBooks(): Flow<List<CollectionWithBooks>>
}