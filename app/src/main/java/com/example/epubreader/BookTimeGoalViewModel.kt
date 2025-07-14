package com.example.epubreader

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.epubreader.model.timeStorage.TimeGoal
import com.example.epubreader.model.timeStorage.TimeGoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimeGoalViewModel(
    private val repository: TimeGoalRepository,
    application: Application,
    ) : AndroidViewModel(application) {

        private val _allTimeGoalBooks = MutableStateFlow<List<TimeGoal>>(emptyList())
        val allTimeGoalBooks: StateFlow<List<TimeGoal>> = _allTimeGoalBooks.asStateFlow()



    init {
        viewModelScope.launch {
            repository.timeGoalBooks.collect { goals ->
                _allTimeGoalBooks.value = goals
            }
        }
    }

    fun updateBookTimeGoal(bookUri: String, timeGoal: Int) {
        viewModelScope.launch{
            repository.updateBookTimeGoal(bookUri, timeGoal)
        }
    }

    suspend fun getTimeGoal(bookUri: String):Int?{
        return repository.getTimeGoal(bookUri, LocalDate.now().dayOfMonth)
    }
    suspend fun getTotalTime(bookUri: String):Long?{
        return repository.getTotalTime(bookUri)
    }

    suspend fun getGoalCompleted(bookUri: String):Int?{
        return repository.getGoalCompleted(bookUri)
    }

    fun deleteBookFromTimeGoal(bookUri: String) {
        viewModelScope.launch {
            val book = repository.getBookByUri(bookUri).firstOrNull()
            if (book != null)
            repository.deleteBook(book)
        }
    }
    fun updateTotalTime(bookUri: String, time: Long) {
        viewModelScope.launch {
            repository.updateTotalTime(bookUri, time+(repository.getBookByUri(bookUri).firstOrNull()!!.totalTime))
        }
    }
    fun resetTotalTime(bookUri: String) {
        viewModelScope.launch {
            repository.updateTotalTime(bookUri, 0)
        }
    }

    fun updateGoalCompleted(bookUri: String, goalCompleted: Int) {
        viewModelScope.launch {
            repository.updateGoalCompleted(bookUri, goalCompleted)
        }
    }

    suspend fun allTimeGoalBooksToMap(bookUri: String): Map<Int, Int> {
        return repository.getAllTimeGoalBooks(bookUri).first().associate { it.date to it.goalCompleted }
    }



    fun addTimeGoalBook(uri: Uri){
        viewModelScope.launch {

            val timeBook = TimeGoal(
                uri = uri.toString(),
                date = LocalDate.now().dayOfMonth,
                totalTime = 0,
                timeGoal = repository.getTimeGoal(bookUri = uri.toString(), date = LocalDate.now().dayOfMonth.minus(1))?:0,
                goalCompleted = 0,
            )
            repository.insertBook(timeBook)
        }
    }

    }

class TimeGoalViewModelFactory(
    private val application: Application,
    private val repository: TimeGoalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeGoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeGoalViewModel(application = application, repository =  repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
