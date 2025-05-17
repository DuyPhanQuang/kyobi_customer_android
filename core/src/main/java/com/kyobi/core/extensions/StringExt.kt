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

// Key duy nhất dựa trên file name, uri hash và token hash
fun String.toUniqueReelCacheKey(): String {
    val fileName = this.substringAfterLast("/").substringBefore("?")
    val hash = this.hashCode()
    val token = this.substringAfter("?token=").takeIf { it.isNotEmpty() } ?: "notoken"
    return "$fileName-$hash-${token.hashCode()}"
}