package com.kyobi.domain.model

data class TrendReview(
    val nickname: String? = null,
    val socialLink: List<String>? = null,
    val medias: List<ShopifyMedia>,
    val product: Product? = null,
    val productId: String? = null,
)