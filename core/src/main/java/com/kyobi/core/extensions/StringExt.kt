package com.kyobi.core.extensions

import kotlinx.serialization.json.Json

fun String.toStringListFromJson(): List<String> {
    return try {
        Json.decodeFromString<List<String>>(this).map { it }
    } catch (e: Exception) {
        emptyList()
    }
}

fun String.toQueryBySingleTag(): String {
    return "tag:$this"
}