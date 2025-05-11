package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetHomepageKeyDataQuery
import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.BannerStatus
import com.kyobi.domain.model.ShopifyMedia

fun mapBanners(
    nodes: List<GetHomepageKeyDataQuery.Node>,
    mediaData: List<ShopifyMedia>
): List<Banner> {
    return nodes.mapNotNull { node ->
        node.onMetaobject?.let { metaobject ->
            try {
                val fieldMap = metaobject.fields.associate { it.key to it.value }
                val imageValue = fieldMap["image"]
                val image = mediaData.find { it.id == imageValue }
                Banner(
                    link = fieldMap["link"] ?: "",
                    order = fieldMap["order"]?.toIntOrNull() ?: 0,
                    tag = fieldMap["tag"] ?: "",
                    title = fieldMap["title"] ?: "",
                    image = image,
                    mobileImage = null,
                    status = fieldMap["status"]?.let {
                        if (it == "active") BannerStatus.ACTIVE else BannerStatus.INACTIVE
                    } ?: BannerStatus.INACTIVE,
                    type = null
                )
            } catch (e: Exception) {
                null
            }
        }
    }.filter { it.status == BannerStatus.ACTIVE }
        .sortedBy { it.order }
}