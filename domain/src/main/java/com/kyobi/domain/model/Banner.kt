package com.kyobi.domain.model

data class Banner(
    val link: String,
    val order: Int,
    val tag: String,
    val title: String,
    val image: ShopifyMedia? = null,
    val mobileImage: ShopifyMedia? = null,
    val status: BannerStatus,
    val type: String?
)

enum class BannerStatus {
    ACTIVE, INACTIVE
}