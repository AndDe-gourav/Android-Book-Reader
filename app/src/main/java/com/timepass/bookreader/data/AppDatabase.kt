package com.timepass.bookreader.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.timepass.bookreader.data.dao.BookCollectionDao
import com.timepass.bookreader.data.dao.BookDao
import com.timepass.bookreader.data.dao.BookStateDao
import com.timepass.bookreader.data.dao.CollectionDao
import com.timepass.bookreader.data.dao.DailyGoalResultDao
import com.timepass.bookreader.data.dao.ReadingGoalDao
import com.timepass.bookreader.data.dao.ReadingSessionDao
import com.timepass.bookreader.data.entity.BookCollectionCrossRef
import com.timepass.bookreader.data.entity.BookEntity
import com.timepass.bookreader.data.entity.BookStateEntity
import com.timepass.bookreader.data.entity.CollectionEntity
import com.timepass.bookreader.data.entity.DailyGoalResultEntity
import com.timepass.bookreader.data.entity.ReadingGoalEntity
import com.timepass.bookreader.data.entity.ReadingSessionEntity

@Database(
    entities = [
        BookEntity::class,
        BookStateEntity::class,
        CollectionEntity::class,
        BookCollectionCrossRef::class,
        ReadingSessionEntity::class,
        ReadingGoalEntity::class,
        DailyGoalResultEntity::class,
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
    abstract fun dailyGoalResultDao(): DailyGoalResultDao

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