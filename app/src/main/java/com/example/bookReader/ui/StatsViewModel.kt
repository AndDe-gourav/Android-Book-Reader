package com.example.bookReader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BookStatEntry(
    val book: BookEntity,
    val todayReadingTimeMs: Long,
    val dailyGoalMinutes: Int?,
    val completedDaysCount: Int = 0
) {
    val isGoalMet: Boolean
        get() {
            val goal = dailyGoalMinutes ?: return false
            return (todayReadingTimeMs / 60_000L) >= goal
        }

    val goalProgress: Float
        get() {
            val goal = dailyGoalMinutes?.takeIf { it > 0 } ?: return 0f
            return ((todayReadingTimeMs / 60_000f) / goal).coerceIn(0f, 1f)
        }
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _booksWithStats = MutableStateFlow<List<BookStatEntry>>(emptyList())
    val booksWithStats: StateFlow<List<BookStatEntry>> = _booksWithStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            // combine() collects all four flows concurrently.
            // Whenever ANY of these tables changes — a book is added, a session is saved,
            // a goal is set, or a daily result is upserted — the block re-executes and
            // the UI updates immediately without any manual refresh() call.
            combine(
                repository.getLibrary(),
                repository.observeSessionChanges(),
                repository.observeGoalChanges(),
                repository.observeDailyResultChanges()
            ) { books, _, _, _ ->
                // We only need the books list to drive loadStats();
                // the other flows are used purely as invalidation signals.
                books
            }.collect { books ->
                loadStats(books)
            }
        }
    }

    private suspend fun loadStats(books: List<BookEntity>) {
        _isLoading.value = true

        val todayStart = todayStartMs()
        val todayEnd   = todayStart + 86_400_000L

        val entries = books.mapNotNull { book ->
            val goal = repository.getReadingGoal(book.bookId) ?: return@mapNotNull null

            val todayTime = runCatching {
                repository.getReadingTimeBetween(book.bookId, todayStart, todayEnd)
            }.getOrDefault(0L)

            val completedDays = runCatching {
                repository.countCompletedDays(book.bookId)
            }.getOrDefault(0)

            BookStatEntry(
                book               = book,
                todayReadingTimeMs = todayTime,
                dailyGoalMinutes   = goal.dailyMinutesGoal,
                completedDaysCount = completedDays
            )
        }

        _booksWithStats.value = entries
        _isLoading.value = false
    }

    // ── Calendar / monthly history ───────────────────────────────────────────

    /**
     * Returns a map of [day-of-month → true/false] for [year]/[month].
     * Reads from the persisted DailyGoalResultEntity records so the calendar
     * correctly reflects the goal that was active on each specific day.
     */
    suspend fun getMonthlyGoalMap(
        bookId: Long,
        year: Int,
        month: Int
    ): Map<Int, Boolean> {
        val (from, to) = monthRangeMs(year, month)
        val results = runCatching {
            repository.getDailyGoalResultsInRange(bookId, from, to)
        }.getOrDefault(emptyList())

        return results.associate { result ->
            val cal = Calendar.getInstance().apply { timeInMillis = result.date }
            cal.get(Calendar.DAY_OF_MONTH) to result.isCompleted
        }
    }


    private fun todayStartMs(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun monthRangeMs(year: Int, month: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
        }.timeInMillis
        return start to end
    }
}