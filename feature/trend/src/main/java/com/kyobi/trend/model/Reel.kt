package com.kyobi.trend.model

data class Reel(
    val id: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val status: String,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val viewCount: Int,
    val createdAt: String,
    val tags: List<String>? = null
)