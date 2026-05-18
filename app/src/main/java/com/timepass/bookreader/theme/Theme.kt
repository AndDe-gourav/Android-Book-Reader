package com.timepass.bookreader.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,

    background = backgroundLight,
    onBackground = onBackgroundLight,

    surface = surfaceLight,
    onSurface = onSurfaceLight,

    tertiary = tertiaryLight
)

@Composable
fun BookReaderTheme(
    content: @Composable () -> Unit
) {

  MaterialTheme(
    colorScheme = lightScheme,
    content = content
  )
}

