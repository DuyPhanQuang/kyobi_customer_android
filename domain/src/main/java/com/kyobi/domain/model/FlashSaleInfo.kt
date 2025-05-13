package com.kyobi.domain.model

data class FlashSaleInfo(
    val id: String,
    val handle: String,
    val type: String,
    val name: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val background: ShopifyImage? = null,
    val productIds: List<String> = emptyList()
)