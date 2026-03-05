package com.example.bookReader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ReadingSessionState(
    val bookId: Long,
    val startTime: Long = System.currentTimeMillis(),
    val startPage: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val sessionTimeSpent: Long = 0L,
    val dailyGoalMinutes: Int? = null,
    /** Previously-saved reading time for TODAY only (not all-time). */
    val todayReadingTimeMs: Long = 0L
)

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<ReadingSessionState?>(null)
    val sessionState: StateFlow<ReadingSessionState?> = _sessionState.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    // ── Session management ────────────────────────────────────────────────────

    fun startSession(bookId: Long, startPage: Int, totalPages: Int) {
        viewModelScope.launch {
            val goal = repository.getReadingGoal(bookId)
            val todayStart = todayStartMs()
            val todayTime = runCatching {
                repository.getReadingTimeBetween(bookId, todayStart, todayStart + 86_400_000L)
            }.getOrDefault(0L)

            _sessionState.value = ReadingSessionState(
                bookId = bookId,
                startTime = System.currentTimeMillis(),
                startPage = startPage,
                currentPage = startPage,
                totalPages = totalPages,
                dailyGoalMinutes = goal?.dailyMinutesGoal,
                todayReadingTimeMs = todayTime
            )
            _currentPage.value = startPage
        }
    }

    fun updatePage(page: Int) {
        _currentPage.value = page
        _sessionState.value = _sessionState.value?.copy(currentPage = page)
    }

    fun updateSessionTime(timeSpent: Long) {
        _sessionState.value = _sessionState.value?.copy(sessionTimeSpent = timeSpent)
    }

    /**
     * End the session, persist it to the DB, then upsert today's DailyGoalResultEntity.
     *
     * The daily result is written AFTER the session row is inserted so the DB query for
     * today's total already includes the just-finished session.
     */
    fun endSession() {
        viewModelScope.launch {
            val state = _sessionState.value ?: return@launch
            // Null out immediately so onCleared() cannot fire a second save.
            _sessionState.value = null

            try {
                // 1. Persist the raw reading session.
                repository.saveSession(
                    bookId = state.bookId,
                    startTime = state.startTime,
                    endTime = System.currentTimeMillis(),
                    startPage = state.startPage,
                    endPage = state.currentPage
                )

                // 2. Update book read-progress / status.
                repository.updateProgress(
                    bookId = state.bookId,
                    page = state.currentPage,
                    totalPages = state.totalPages
                )

                // 3. Upsert today's goal result (only when a goal has been set).
                val goalMinutes = state.dailyGoalMinutes ?: return@launch
                val todayStart = todayStartMs()
                // Re-query the DB so the total includes the session just saved above.
                val minutesReadToday = repository.getReadingTimeBetween(
                    bookId = state.bookId,
                    from  = todayStart,
                    to    = todayStart + 86_400_000L
                ) / 60_000L

                repository.saveDailyGoalResult(
                    bookId     = state.bookId,
                    dayStartMs = todayStart,
                    goalMinutes = goalMinutes,
                    minutesRead = minutesReadToday
                )
            } catch (e: Exception) {
                // Surface via a SharedFlow<UiEvent> if you want error snackbars
            }
        }
    }

    /**
     * Called by the UI timer when midnight is detected mid-session.
     *
     * Responsibilities:
     *  1. Persists a DailyGoalResultEntity for the day that just ended, using
     *     [minutesReadBeforeMidnight] as the final tally for that day.
     *  2. Resets todayReadingTimeMs and sessionTimeSpent to 0 in the session
     *     state so the progress bar starts fresh for the new day.
     *
     * The caller (PdfReaderScreen timer loop) is responsible for resetting its
     * own local accumulatedSessionTime / sessionPeriodStart AFTER this call.
     */
    fun onMidnightCrossed(prevDayStartMs: Long, minutesReadBeforeMidnight: Long) {
        viewModelScope.launch {
            val state = _sessionState.value ?: return@launch
            val goalMinutes = state.dailyGoalMinutes

            if (goalMinutes != null) {
                repository.saveDailyGoalResult(
                    bookId      = state.bookId,
                    dayStartMs  = prevDayStartMs,
                    goalMinutes = goalMinutes,
                    minutesRead = minutesReadBeforeMidnight
                )
            }

            // New day — clear accumulators so isGoalMet / getGoalProgress reflect today.
            _sessionState.value = state.copy(
                todayReadingTimeMs = 0L,
                sessionTimeSpent   = 0L
            )
        }
    }

    // ── Goal helpers ──────────────────────────────────────────────────────────

    fun setReadingGoal(bookId: Long, dailyMinutes: Int) {
        viewModelScope.launch {
            try {
                repository.setReadingGoal(bookId, dailyMinutes)
                _sessionState.value = _sessionState.value?.copy(dailyGoalMinutes = dailyMinutes)
            } catch (e: Exception) { /* handle */ }
        }
    }

    suspend fun getReadingGoal(bookId: Long): Int? = runCatching {
        repository.getReadingGoal(bookId)?.dailyMinutesGoal
    }.getOrNull()

    suspend fun getTotalReadingTime(bookId: Long): Long = runCatching {
        repository.getTotalReadingTime(bookId)
    }.getOrDefault(0L)

    /** True when today's reading (saved sessions + current session) meets the daily goal. */
    fun isGoalMet(): Boolean {
        val state = _sessionState.value ?: return false
        val goalMinutes = state.dailyGoalMinutes ?: return false
        val totalMinutes = (state.todayReadingTimeMs + state.sessionTimeSpent) / 60_000L
        return totalMinutes >= goalMinutes
    }

    /** Progress towards today's daily goal, clamped to [0, 1]. */
    fun getGoalProgress(): Float {
        val state = _sessionState.value ?: return 0f
        val goalMinutes = state.dailyGoalMinutes?.takeIf { it > 0 } ?: return 0f
        val totalMinutes = (state.todayReadingTimeMs + state.sessionTimeSpent) / 60_000f
        return (totalMinutes / goalMinutes).coerceIn(0f, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        // Safe: endSession() no-ops if _sessionState is already null.
        endSession()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Epoch-ms of midnight (00:00:00.000) for today in the device's local timezone. */
    fun todayStartMs(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}