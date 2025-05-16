package com.kyobi.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "preloaded_media",
    indices = [
        Index(value = ["url"], unique = true)
    ]
)
data class PreloadedMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val cacheKey: String,
    val timestamp: Long
)