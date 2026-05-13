package com.timepass.bookreader.ui

sealed class UiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null
    ) : UiEvent()
}