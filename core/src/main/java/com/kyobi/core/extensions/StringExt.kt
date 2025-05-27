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

// generate unique key based on file name, uri hash và token hash
fun String.toUniqueReelCacheKey(): String {
    val fileName = this.substringAfterLast("/").substringBefore("?")
    val hash = this.hashCode()
    val token = this.substringAfter("?token=").takeIf { it.isNotEmpty() } ?: "notoken"
    return "$fileName-$hash-${token.hashCode()}"
}

fun String?.toFirstGid(): String? {
    if (this == null) return null
    return try {
        val ids = Json.decodeFromString<List<String>>(this)
        ids.firstOrNull()
    } catch (e: Exception) {
        null
    }
}

fun String?.toGids(): List<String> {
    if (this == null) return emptyList()
    return try {
        Json.decodeFromString<List<String>>(this)
    } catch (e: Exception) {
        emptyList()
    }
}

fun String?.toNullIfStringNull(): String? {
    return if (this == "null") null else this
}

fun String.getCollectionNameFromHandle(): String {
    return this
        .split("-")
        .joinToString(" ") { word -> word[0].uppercaseChar() + word.substring(1) }
}

fun String.toUppercaseFirstChar(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}