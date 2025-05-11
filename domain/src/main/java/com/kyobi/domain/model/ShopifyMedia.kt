package com.kyobi.domain.model

data class ShopifyVideoPreviewImage(
    val url: String,
)

enum class ShopifySourceFormatType { mp4, m3u8 }

data class ShopifySource(
    val format: ShopifySourceFormatType,
    val url: String,
)

data class ShopifyMedia(
    val id: String,
    val image: ShopifyImage? = null, //for image
    val previewImage: ShopifyVideoPreviewImage? = null, //for video
    val sources: List<ShopifySource>? = null, //for video
)