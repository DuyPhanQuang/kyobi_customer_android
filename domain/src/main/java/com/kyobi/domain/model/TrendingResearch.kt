package com.kyobi.domain.model

data class TrendingResearch(
    val title: String,
    val label: String,
    val descriptionHtml: String,
    val thumbnail: ShopifyMedia? = null,
    val allMedias: List<ShopifyMedia> = emptyList(),
    val link: String,
    val tag: String,
    val order: Int,
    val hashtag: List<String> = emptyList(),
    val trendReviewIds: List<String> = emptyList(),
) {
    companion object {
        fun empty() = TrendingResearch(
            title = "",
            label = "",
            descriptionHtml = "",
            link = "",
            tag = "",
            order = 0
        )
    }
}