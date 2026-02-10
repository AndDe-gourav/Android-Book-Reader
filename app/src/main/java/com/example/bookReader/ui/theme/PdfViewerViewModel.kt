package com.example.bookReader.ui.theme


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.dao.ReadingGoalDao
import com.example.bookReader.data.dao.ReadingSessionDao
import com.example.bookReader.data.entity.ReadingGoalEntity
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReadingSessionState(
    val bookId: Long,
    val startTime: Long = System.currentTimeMillis(),
    val startPage: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val sessionTimeSpent: Long = 0L,
    val dailyGoalMinutes: Int? = null,
    val totalReadingTime: Long = 0L
)

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    private val repository: BookRepository,
    private val sessionDao: ReadingSessionDao,
    private val goalDao: ReadingGoalDao
) : ViewModel() {

    private val _sessionState = MutableStateFlow<ReadingSessionState?>(null)
    val sessionState: StateFlow<ReadingSessionState?> = _sessionState.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    /**
     * Start a new reading session
     */
    fun startSession(bookId: Long, startPage: Int, totalPages: Int) {
        viewModelScope.launch {
            val goal = goalDao.getGoal(bookId)
            val totalTime = sessionDao.getTotalReadingTime(bookId)

            _sessionState.value = ReadingSessionState(
                bookId = bookId,
                startTime = System.currentTimeMillis(),
                startPage = startPage,
                currentPage = startPage,
                totalPages = totalPages,
                dailyGoalMinutes = goal?.dailyMinutesGoal,
                totalReadingTime = totalTime
            )
            _currentPage.value = startPage
        }
    }

    /**
     * Update current page
     */
    fun updatePage(page: Int) {
        _currentPage.value = page
        _sessionState.value = _sessionState.value?.copy(currentPage = page)
    }

    /**
     * Update session time spent
     */
    fun updateSessionTime(timeSpent: Long) {
        _sessionState.value = _sessionState.value?.copy(sessionTimeSpent = timeSpent)
    }

    /**
     * End the current reading session and save it
     */
    fun endSession() {
        viewModelScope.launch {
            val state = _sessionState.value ?: return@launch

            try {
                // Save the reading session
                repository.saveSession(
                    bookId = state.bookId,
                    startTime = state.startTime,
                    endTime = System.currentTimeMillis(),
                    startPage = state.startPage,
                    endPage = state.currentPage
                )

                // Update book progress
                repository.updateProgress(
                    bookId = state.bookId,
                    page = state.currentPage,
                    totalPages = state.totalPages
                )
            } catch (e: Exception) {
                // Handle error silently or log it
            } finally {
                _sessionState.value = null
            }
        }
    }

    /**
     * Set a daily reading goal for a book
     */
    fun setReadingGoal(bookId: Long, dailyMinutes: Int) {
        viewModelScope.launch {
            try {
                goalDao.setGoal(
                    ReadingGoalEntity(
                        bookId = bookId,
                        dailyMinutesGoal = dailyMinutes
                    )
                )
                _sessionState.value = _sessionState.value?.copy(dailyGoalMinutes = dailyMinutes)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Get reading goal for a book
     */
    suspend fun getReadingGoal(bookId: Long): Int? {
        return try {
            goalDao.getGoal(bookId)?.dailyMinutesGoal
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get total reading time for a book
     */
    suspend fun getTotalReadingTime(bookId: Long): Long {
        return try {
            sessionDao.getTotalReadingTime(bookId) ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Check if daily goal is met
     */
    fun isGoalMet(): Boolean {
        val state = _sessionState.value ?: return false
        val goalMinutes = state.dailyGoalMinutes ?: return false
        val totalMinutes = (state.totalReadingTime + state.sessionTimeSpent) / 60000
        return totalMinutes >= goalMinutes
    }

    /**
     * Get progress percentage towards daily goal
     */
    fun getGoalProgress(): Float {
        val state = _sessionState.value ?: return 0f
        val goalMinutes = state.dailyGoalMinutes ?: return 0f
        val totalMinutes = (state.totalReadingTime + state.sessionTimeSpent) / 60000
        return (totalMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        // Auto-save session when ViewModel is cleared
        endSession()
    }
}