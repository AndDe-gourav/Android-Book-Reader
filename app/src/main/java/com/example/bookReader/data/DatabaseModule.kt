package com.example.bookReader.data


import android.content.Context
import androidx.room.Room
import com.example.bookReader.data.dao.BookCollectionDao
import com.example.bookReader.data.dao.BookDao
import com.example.bookReader.data.dao.BookStateDao
import com.example.bookReader.data.dao.CollectionDao
import com.example.bookReader.data.dao.ReadingGoalDao
import com.example.bookReader.data.dao.ReadingSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "reader.db"
        ).build()

    @Provides
    @Singleton
    fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()

    @Provides
    @Singleton
    fun provideBookStateDao(db: AppDatabase): BookStateDao = db.bookStateDao()

    @Provides
    @Singleton
    fun provideCollectionDao(db: AppDatabase): CollectionDao = db.collectionDao()

    @Provides
    @Singleton
    fun provideBookCollectionDao(db: AppDatabase): BookCollectionDao = db.bookCollectionDao()

    @Provides
    @Singleton
    fun provideReadingSessionDao(db: AppDatabase): ReadingSessionDao = db.sessionDao()

    @Provides
    @Singleton
    fun provideReadingGoalDao(db: AppDatabase): ReadingGoalDao = db.goalDao()
}