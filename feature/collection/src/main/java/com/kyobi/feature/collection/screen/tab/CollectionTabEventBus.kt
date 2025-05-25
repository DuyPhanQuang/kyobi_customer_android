package com.kyobi.feature.collection.screen.tab

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

sealed class CollectionTabEvent {
    data class CategorySelected(val filterHandle: String?): CollectionTabEvent()
    data class SubCategorySelected(val filterHandle: String?): CollectionTabEvent()
    data class RefreshTriggered(val filterHandle: String?): CollectionTabEvent()
}

@ViewModelScoped
class CollectionTabEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<CollectionTabEvent>(replay = 0)
    val events: SharedFlow<CollectionTabEvent> = _events

    suspend fun emitEvent(event: CollectionTabEvent) {
        _events.emit(event)
    }
}