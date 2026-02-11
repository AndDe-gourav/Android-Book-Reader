package com.example.bookReader.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookReader.data.dao.BookCollectionDao
import com.example.bookReader.data.dao.BookDao
import com.example.bookReader.data.dao.BookStateDao
import com.example.bookReader.data.dao.CollectionDao
import com.example.bookReader.data.dao.ReadingGoalDao
import com.example.bookReader.data.dao.ReadingSessionDao
import com.example.bookReader.data.entity.BookCollectionCrossRef
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.entity.BookStateEntity
import com.example.bookReader.data.entity.CollectionEntity
import com.example.bookReader.data.entity.ReadingGoalEntity
import com.example.bookReader.data.entity.ReadingSessionEntity

@Database(
    entities = [
        BookEntity::class,
        BookStateEntity::class,
        CollectionEntity::class,
        BookCollectionCrossRef::class,
        ReadingSessionEntity::class,
        ReadingGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun bookStateDao(): BookStateDao
    abstract fun collectionDao(): CollectionDao
    abstract fun bookCollectionDao(): BookCollectionDao
    abstract fun sessionDao(): ReadingSessionDao
    abstract fun goalDao(): ReadingGoalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reader.db"
                ).build().also { INSTANCE = it }
            }
    }
}