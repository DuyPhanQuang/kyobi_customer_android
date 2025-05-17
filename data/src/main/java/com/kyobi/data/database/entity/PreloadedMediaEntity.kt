package com.kyobi.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "preloaded_media",
)
data class PreloadedMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val cacheKey: String,
    val tsCacheKeys: List<String> = emptyList(),
    val timestamp: Long
)