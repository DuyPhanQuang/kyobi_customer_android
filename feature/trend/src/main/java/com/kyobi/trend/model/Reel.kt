package com.kyobi.trend.model

data class Reel(
    val id: String,
    val userId: String,
    val videoUrl: String,
    val text: String,
    val musicUrl: String? = null,
    val parentReelId: String? = null,
    val shortenUrl: String,
    val thumbnailUrl: String,
    val thumbnailGif: String,
    val status: String,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val viewCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val originalDuration: Double,
    val shortenDuration: Double,
    val tags: List<String> = emptyList()
)