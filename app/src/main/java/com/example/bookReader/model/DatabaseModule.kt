package com.example.bookReader.model

import android.content.Context
import com.example.bookReader.data.AppDatabase
import com.example.bookReader.data.dao.BookCollectionDao
import com.example.bookReader.data.dao.BookDao
import com.example.bookReader.data.dao.BookStateDao
import com.example.bookReader.data.dao.CollectionDao
import com.example.bookReader.data.dao.ReadingGoalDao
import com.example.bookReader.data.dao.ReadingSessionDao
import com.example.bookReader.data.repository.BookRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.get(context)
    }

    @Provides
    @Singleton
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    @Singleton
    fun provideBookStateDao(database: AppDatabase): BookStateDao {
        return database.bookStateDao()
    }

    @Provides
    @Singleton
    fun provideCollectionDao(database: AppDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    @Singleton
    fun provideBookCollectionDao(database: AppDatabase): BookCollectionDao {
        return database.bookCollectionDao()
    }

    @Provides
    @Singleton
    fun provideReadingSessionDao(database: AppDatabase): ReadingSessionDao {
        return database.sessionDao()
    }

    @Provides
    @Singleton
    fun provideReadingGoalDao(database: AppDatabase): ReadingGoalDao {
        return database.goalDao()
    }

    @Provides
    @Singleton
    fun provideBookRepository(
        bookDao: BookDao,
        bookStateDao: BookStateDao,
        collectionDao: CollectionDao,
        bookCollectionDao: BookCollectionDao,
        sessionDao: ReadingSessionDao,
        goalDao: ReadingGoalDao,
        @ApplicationContext context: Context
    ): BookRepository {
        return BookRepository(
            bookDao = bookDao,
            bookStateDao = bookStateDao,
            collectionDao = collectionDao,
            bookCollectionDao = bookCollectionDao,
            sessionDao = sessionDao,
            goalDao = goalDao,
            context = context
        )
    }
}