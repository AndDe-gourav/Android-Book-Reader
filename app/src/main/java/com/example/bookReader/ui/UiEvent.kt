package com.example.bookReader.ui

sealed class UiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null
    ) : UiEvent()
}