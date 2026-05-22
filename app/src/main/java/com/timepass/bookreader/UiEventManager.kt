package com.timepass.bookreader

import com.timepass.bookreader.ui.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object UiEventManager {

    private val _events = MutableSharedFlow<UiEvent>()

    val events = _events.asSharedFlow()

    suspend fun sendEvent(event: UiEvent) {
        _events.emit(event)
    }
}