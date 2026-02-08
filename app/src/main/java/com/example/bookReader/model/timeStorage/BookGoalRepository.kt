package com.example.bookReader.model.timeStorage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class TimeGoalRepository(private val timeGoalDao: TimeGoalDao) {

    val timeGoalBooks: Flow<List<TimeGoal>> = timeGoalDao.getTimeGoalBooks(date = LocalDate.now().dayOfMonth)

    suspend fun getTotalTime(bookUri: String):Long?{
        return timeGoalDao.getBookByUri(bookUri, LocalDate.now().dayOfMonth).firstOrNull()?.totalTime
    }

    fun getAllTimeGoalBooks(bookUri: String): Flow<List<TimeGoal>> {
        return timeGoalDao.getAllTimeGoalBooks(bookUri)
    }
    suspend fun getTimeGoal(bookUri: String, date: Int):Int?{
        return  timeGoalDao.getBookByUri(bookUri, date).firstOrNull()?.timeGoal
    }

    suspend fun getGoalCompleted(bookUri: String):Int?{
        return  timeGoalDao.getBookByUri(bookUri, LocalDate.now().dayOfMonth).firstOrNull()?.goalCompleted
    }

    fun  getBookByUri(bookUri: String): Flow<TimeGoal?>{
        return  timeGoalDao.getBookByUri(bookUri, LocalDate.now().dayOfMonth)
    }
    suspend fun updateBookTimeGoal(bookUri: String, time: Int) {
        timeGoalDao.updateBookTimeGoal(bookUri, time, LocalDate.now().dayOfMonth)
    }

    suspend fun deleteBook(book: TimeGoal) {
        timeGoalDao.deleteBook(book)
    }

    suspend fun updateTotalTime(bookUri: String, totalTime: Long) {
        timeGoalDao.updateTotalTime(bookUri, totalTime, LocalDate.now().dayOfMonth)
    }


    suspend fun updateGoalCompleted(bookUri: String, goalCompleted: Int) {
        timeGoalDao.updateGoalCompleted(bookUri, goalCompleted, LocalDate.now().dayOfMonth)
    }

    suspend fun insertBook(book: TimeGoal): Long? {
        val existingBook = timeGoalDao.getBookByUriAndDate(book.uri, LocalDate.now().dayOfMonth).firstOrNull()
        return if (existingBook == null) {
            timeGoalDao.insertBook(book = book)
        } else {
            null
        }
    }
}