package com.kyobi.feature.collection

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class CollectionTabEvent {
    data class CategorySelected(val filterHandle: String?): CollectionTabEvent()
    data class SubCategorySelected(val filterHandle: String?): CollectionTabEvent()
}

@Singleton
class CollectionTabEventBus @Inject constructor() {
    private val _collectionTabEvents = MutableSharedFlow<CollectionTabEvent>(replay = 0)
    val collectionTabEvents: SharedFlow<CollectionTabEvent> = _collectionTabEvents

    suspend fun emitCollectionTabEvent(event: CollectionTabEvent) {
        _collectionTabEvents.emit(event)
    }
}