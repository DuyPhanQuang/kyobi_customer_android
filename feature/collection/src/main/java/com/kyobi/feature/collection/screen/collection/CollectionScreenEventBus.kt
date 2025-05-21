package com.kyobi.feature.collection.screen.collection

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class CollectionScreenEvent {
    data class CollectionSelected(val filterHandle: String?): CollectionScreenEvent()
}

class CollectionScreenEventBus {
    private val _events = MutableSharedFlow<CollectionScreenEvent>(replay = 0)
    val events: SharedFlow<CollectionScreenEvent> = _events

    suspend fun emitEvent(event: CollectionScreenEvent) {
        _events.emit(event)
    }
}