package com.kyobi.domain.model

data class TrendReview(
    val nickname: String? = null,
    val socialLink: List<String>? = emptyList(),
    val medias: List<ShopifyMedia>? = emptyList(),
    val product: Product? = null,
    val productId: String? = null,
)