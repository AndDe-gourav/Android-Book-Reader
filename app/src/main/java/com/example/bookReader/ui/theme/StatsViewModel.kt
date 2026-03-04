package com.example.bookReader.ui.theme


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookReader.data.entity.BookEntity
import com.example.bookReader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Summary of a single book's reading stats — shown as one card in StatsScreen.
 */
data class BookStatEntry(
    val book: BookEntity,
    /** Total accumulated reading time in milliseconds. */
    val totalReadingTimeMs: Long,
    /** Daily goal in minutes, or null if no goal has been set. */
    val dailyGoalMinutes: Int?
) {
    /** True when total reading time meets or exceeds the daily goal. */
    val isGoalMet: Boolean
        get() {
            val goal = dailyGoalMinutes ?: return false
            return (totalReadingTimeMs / 60_000L) >= goal
        }

    /** Progress towards the daily goal, clamped to [0, 1]. */
    val goalProgress: Float
        get() {
            val goal = dailyGoalMinutes?.takeIf { it > 0 } ?: return 0f
            return ((totalReadingTimeMs / 60_000f) / goal).coerceIn(0f, 1f)
        }
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    // ── Books with goals ─────────────────────────────────────────────────────

    /**
     * All books that have a reading goal set, enriched with their stats.
     * Emits a new list whenever the library changes.
     */
    private val _booksWithStats = MutableStateFlow<List<BookStatEntry>>(emptyList())
    val booksWithStats: StateFlow<List<BookStatEntry>> = _booksWithStats.asStateFlow()

    /** Whether stats are currently being loaded. */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // React to library changes and reload stats each time
        viewModelScope.launch {
            repository.getLibrary().collect { books ->
                loadStats(books)
            }
        }
    }

    private suspend fun loadStats(books: List<BookEntity>) {
        _isLoading.value = true
        val entries = books.mapNotNull { book ->
            val goal = repository.getReadingGoal(book.bookId)
            // Only include books that have a goal set
            if (goal == null) return@mapNotNull null
            val totalTime = runCatching {
                repository.getTotalReadingTime(book.bookId)
            }.getOrDefault(0L)
            BookStatEntry(
                book = book,
                totalReadingTimeMs = totalTime,
                dailyGoalMinutes = goal.dailyMinutesGoal
            )
        }
        _booksWithStats.value = entries
        _isLoading.value = false
    }

    /** Force a refresh (e.g. after a session ends). */
    fun refresh() {
        viewModelScope.launch {
            val books = _booksWithStats.value.map { it.book }
            loadStats(books)
        }
    }

    // ── Calendar / monthly history ───────────────────────────────────────────

    /**
     * Returns a map of [day-of-month → true/false] for the given [year]/[month],
     * where true means the daily reading goal was met on that day.
     *
     * Days with no sessions are absent from the map.
     */
    suspend fun getMonthlyGoalMap(
        bookId: Long,
        year: Int,
        month: Int,
        dailyGoalMinutes: Int
    ): Map<Int, Boolean> {
        val (from, to) = monthRangeMs(year, month)
        val sessions = runCatching {
            repository.getSessionsBetween(from, to)
        }.getOrDefault(emptyList())

        // Group sessions by calendar day and accumulate duration
        val minutesPerDay = mutableMapOf<Int, Long>()
        sessions
            .filter { it.bookId == bookId }
            .forEach { session ->
                val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val durationMs = session.endTime - session.startTime
                minutesPerDay[day] = (minutesPerDay[day] ?: 0L) + (durationMs / 60_000L)
            }

        return minutesPerDay.mapValues { (_, minutes) -> minutes >= dailyGoalMinutes }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun monthRangeMs(year: Int, month: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
        }.timeInMillis
        return start to end
    }
}