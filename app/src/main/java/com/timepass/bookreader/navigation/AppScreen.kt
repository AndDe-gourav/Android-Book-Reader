package com.timepass.bookreader.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object HomeScreen : NavKey

@Serializable
object StatsScreen : NavKey

@Serializable
object EditScreen : NavKey

@Serializable
object AboutBookScreen : NavKey

@Serializable
data class PdfReader(
    val bookId: Long
) : NavKey