package com.example.bookReader.model.timeStorage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TimeGoal::class], version = 1, exportSchema = false)
abstract class TimeGoalDatabase : RoomDatabase() {
    abstract fun TimeGoalDao(): TimeGoalDao

    companion object {
        @Volatile
        private var INSTANCE: TimeGoalDatabase? = null

        fun getDatabase(context: Context): TimeGoalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                TimeGoalDatabase::class.java,
                                "time_goal_database"
                            ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
