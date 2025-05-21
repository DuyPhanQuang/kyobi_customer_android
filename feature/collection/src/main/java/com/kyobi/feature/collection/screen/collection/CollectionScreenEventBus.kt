package com.kyobi.feature.collection.screen.collection

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import javax.inject.Inject

sealed class CollectionScreenEvent {
    data class CollectionSelected(val filterHandle: String?): CollectionScreenEvent()
}

@ViewModelScoped
class CollectionScreenEventBus @Inject constructor() {
    private val tag = "CollectionScreenEventBus"
    private val _events = MutableSharedFlow<CollectionScreenEvent>(replay = 0)
    val events: SharedFlow<CollectionScreenEvent> = _events

    suspend fun emitEvent(event: CollectionScreenEvent) {
        _events.emit(event)
    }

    @Suppress("ProtectedInFinal")
    protected fun finalize() {
        Timber.tag(tag).d("EventBus garbage collected: $this")
    }
}