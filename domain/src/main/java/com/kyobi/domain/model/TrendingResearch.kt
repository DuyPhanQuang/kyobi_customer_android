package com.kyobi.domain.model

data class TrendingResearch(
    val title: String,
    val label: String,
    val descriptionHtml: String,
    val thumbnail: ShopifyMedia? = null,
    val allMedias: List<ShopifyMedia>? = null,
    val link: String,
    val tag: String,
    val order: Int,
    val hashtag: List<String>? = null,
    val trendReviewIds: List<String>? = null,
)