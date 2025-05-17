package com.kyobi.data.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class AppTypeConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Json.decodeFromString(value)
    }
}