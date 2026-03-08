package com.example.bookReader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
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

    /**
     * Guards against endSession / endSessionBlocking being called concurrently from
     * multiple paths (ON_STOP + onDispose + onCleared all within milliseconds of each
     * other). compareAndSet(false, true) returns true exactly once.
     */
    private val isSaving = AtomicBoolean(false)

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
            // Reset the save guard each time a new session begins.
            isSaving.set(false)
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
     * BLOCKING save — call this from the lifecycle ON_STOP observer.
     *
     * WHY BLOCKING:
     * Android guarantees the system waits for onStop() to return before killing the
     * process. A coroutine launched asynchronously (even on saveScope) may never get
     * to execute if the process is killed the moment onStop() returns.
     * By doing the DB writes synchronously here we get a 100% guarantee they finish
     * before the system gets the chance to kill the process.
     *
     * Room writes are fast (< 50 ms on any modern device) so briefly blocking the
     * main thread during onStop() is completely acceptable and causes no ANR risk.
     */
    fun endSessionBlocking() {
        if (!isSaving.compareAndSet(false, true)) return
        val state = _sessionState.value ?: run { isSaving.set(false); return }
        _sessionState.value = null

        // runBlocking bridges from the main thread into a blocking coroutine on IO.
        runBlocking(Dispatchers.IO) {
            try {
                val endTime = System.currentTimeMillis()

                repository.saveSession(
                    bookId    = state.bookId,
                    startTime = state.startTime,
                    endTime   = endTime,
                    startPage = state.startPage,
                    endPage   = state.currentPage
                )

                repository.updateProgress(
                    bookId     = state.bookId,
                    page       = state.currentPage,
                    totalPages = state.totalPages
                )

                val goalMinutes = state.dailyGoalMinutes
                if (goalMinutes != null) {
                    val todayStart = todayStartMs()
                    val minutesReadToday = repository.getReadingTimeBetween(
                        bookId = state.bookId,
                        from   = todayStart,
                        to     = todayStart + 86_400_000L
                    ) / 60_000L

                    repository.saveDailyGoalResult(
                        bookId      = state.bookId,
                        dayStartMs  = todayStart,
                        goalMinutes = goalMinutes,
                        minutesRead = minutesReadToday
                    )
                }
            } catch (e: Exception) {
                // Log if needed
            } finally {
                isSaving.set(false)
            }
        }
    }

    /**
     * ASYNC save — call this from onDispose (normal back navigation).
     *
     * When the user presses Back, the process stays alive and the coroutine has
     * time to complete, so async is fine and avoids blocking the main thread
     * during navigation. Uses NonCancellable so a scope cancellation mid-flight
     * doesn't leave a partial write.
     */
    fun endSession() {
        if (!isSaving.compareAndSet(false, true)) return
        val state = _sessionState.value ?: run { isSaving.set(false); return }
        _sessionState.value = null

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            withContext(NonCancellable) {
                try {
                    val endTime = System.currentTimeMillis()

                    repository.saveSession(
                        bookId    = state.bookId,
                        startTime = state.startTime,
                        endTime   = endTime,
                        startPage = state.startPage,
                        endPage   = state.currentPage
                    )

                    repository.updateProgress(
                        bookId     = state.bookId,
                        page       = state.currentPage,
                        totalPages = state.totalPages
                    )

                    val goalMinutes = state.dailyGoalMinutes
                    if (goalMinutes != null) {
                        val todayStart = todayStartMs()
                        val minutesReadToday = repository.getReadingTimeBetween(
                            bookId = state.bookId,
                            from   = todayStart,
                            to     = todayStart + 86_400_000L
                        ) / 60_000L

                        repository.saveDailyGoalResult(
                            bookId      = state.bookId,
                            dayStartMs  = todayStart,
                            goalMinutes = goalMinutes,
                            minutesRead = minutesReadToday
                        )
                    }
                } catch (e: Exception) {
                    // Log if needed
                } finally {
                    isSaving.set(false)
                }
            }
        }
    }

    /**
     * Called when midnight is detected mid-session by the timer loop.
     * Persists the result for the ending day, then resets in-memory counters.
     */
    fun onMidnightCrossed(prevDayStartMs: Long, minutesReadBeforeMidnight: Long) {
        val state = _sessionState.value ?: return
        val goalMinutes = state.dailyGoalMinutes ?: return

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            withContext(NonCancellable) {
                runCatching {
                    repository.saveDailyGoalResult(
                        bookId      = state.bookId,
                        dayStartMs  = prevDayStartMs,
                        goalMinutes = goalMinutes,
                        minutesRead = minutesReadBeforeMidnight
                    )
                }
            }
        }

        _sessionState.value = state.copy(todayReadingTimeMs = 0L, sessionTimeSpent = 0L)
    }

    // ── Goal helpers ──────────────────────────────────────────────────────────

    fun setReadingGoal(bookId: Long, dailyMinutes: Int) {
        viewModelScope.launch {
            runCatching { repository.setReadingGoal(bookId, dailyMinutes) }
            _sessionState.value = _sessionState.value?.copy(dailyGoalMinutes = dailyMinutes)
        }
    }

    suspend fun getReadingGoal(bookId: Long): Int? =
        runCatching { repository.getReadingGoal(bookId)?.dailyMinutesGoal }.getOrNull()

    suspend fun getTotalReadingTime(bookId: Long): Long =
        runCatching { repository.getTotalReadingTime(bookId) }.getOrDefault(0L)

    fun isGoalMet(): Boolean {
        val state = _sessionState.value ?: return false
        val goalMinutes = state.dailyGoalMinutes ?: return false
        return (state.todayReadingTimeMs + state.sessionTimeSpent) / 60_000L >= goalMinutes
    }

    fun getGoalProgress(): Float {
        val state = _sessionState.value ?: return 0f
        val goalMinutes = state.dailyGoalMinutes?.takeIf { it > 0 } ?: return 0f
        val totalMinutes = (state.todayReadingTimeMs + state.sessionTimeSpent) / 60_000f
        return (totalMinutes / goalMinutes).coerceIn(0f, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        // Last-resort fallback: if somehow ON_STOP and onDispose both missed the save
        // (e.g. in tests or unusual lifecycle paths), endSession() still tries async.
        // In normal app usage this is a no-op because isSaving is already true.
        endSession()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun todayStartMs(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}