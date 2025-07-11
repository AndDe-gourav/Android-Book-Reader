package com.example.epubreader.model.timeStorage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class TimeGoalRepository(private val timeGoalDao: TimeGoalDao) {

    val timeGoalBooks: Flow<List<TimeGoal>> = timeGoalDao.getTimeGoalBooks()

    suspend fun getTotalTime(bookUri: String):Long?{
        return timeGoalDao.getBookByUri(bookUri).firstOrNull()?.totalTime
    }

    suspend fun getTimeGoal(bookUri: String):Int?{
        return  timeGoalDao.getBookByUri(bookUri).firstOrNull()?.timeGoal
    }

    suspend fun getGoalCompleted(bookUri: String):Int?{
        return  timeGoalDao.getBookByUri(bookUri).firstOrNull()?.goalCompleted
    }

    fun  getBookByUri(bookUri: String): Flow<TimeGoal?>{
        return  timeGoalDao.getBookByUri(bookUri)
    }
    suspend fun updateBookTimeGoal(bookUri: String, time: Int) {
        timeGoalDao.updateBookTimeGoal(bookUri, time)
    }

    suspend fun deleteBook(book: TimeGoal) {
        timeGoalDao.deleteBook(book)
    }

    suspend fun updateTotalTime(bookUri: String, totalTime: Long) {
        timeGoalDao.updateTotalTime(bookUri, totalTime)
    }

    suspend fun updateGoalCompleted(bookUri: String, goalCompleted: Int) {
        timeGoalDao.updateGoalCompleted(bookUri, goalCompleted)
    }



    suspend fun insertBook(book: TimeGoal): Long? {
        val existingBook = timeGoalDao.getBookByUri(book.uri).firstOrNull()
        return if (existingBook == null) {
            timeGoalDao.insertBook(book = book)
        } else {
            null
        }
    }
}