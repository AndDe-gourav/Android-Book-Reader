package com.example.bookReader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookReader.data.entity.BookCollectionCrossRef

@Dao
interface BookCollectionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookToCollection(ref: BookCollectionCrossRef)

    @Query("""
        DELETE FROM book_collection_cross_ref
        WHERE bookId = :bookId AND collectionId = :collectionId
    """)
    suspend fun removeBookFromCollection(bookId: Long, collectionId: Long)
}
