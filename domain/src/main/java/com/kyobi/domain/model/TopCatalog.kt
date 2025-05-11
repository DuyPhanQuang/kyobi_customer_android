package com.kyobi.domain.model

data class TopCatalog(
    val link: String,
    val order: Int,
    val tag: String,
    val title: String,
    val image: ShopifyMedia? = null,
    val status: TopCatalogStatus
)

enum class TopCatalogStatus { ACTIVE, INACTIVE }