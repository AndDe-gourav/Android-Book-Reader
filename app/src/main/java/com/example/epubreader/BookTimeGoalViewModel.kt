package com.example.epubreader

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.epubreader.model.timeStorage.TimeGoal
import com.example.epubreader.model.timeStorage.TimeGoalRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimeGoalViewModel(
    private val repository: TimeGoalRepository,
    application: Application,
    ) : AndroidViewModel(application) {

    fun updateBookTimeGoal(bookUri: String, timeGoal: Int) {
        viewModelScope.launch{
            repository.updateBookTimeGoal(bookUri, timeGoal)
        }
    }

    suspend fun getTimeGoal(bookUri: String): Int?{
        return repository.getTimeGoal(bookUri)
    }

    fun deleteBookFromTimeGoal(bookUri: String) {
        viewModelScope.launch {
            val book = repository.getBookByUri(bookUri).firstOrNull()!!
            repository.deleteBook(book)
        }
    }

    fun updateStartTime(bookUri: String, startTime: Long) {
        viewModelScope.launch {
            repository.updateStartTime(bookUri, startTime)
        }
    }

    fun updateTotalTime(bookUri: String, endTime: Long) {
        viewModelScope.launch {
            val totalTime =
                (repository.getTotalTime(bookUri) ?: 0) + endTime - repository.getStartingTime(bookUri)!!
            repository.updateTotalTime(bookUri, totalTime)
        }
    }

    fun addTimeGoalBook(uri: Uri){
        viewModelScope.launch {

            val timeBook = TimeGoal(
                uri = uri.toString(),
                date = LocalDate.now().toString(),
                startTime = 0,
                totalTime = 0,
                timeGoal = 0
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
