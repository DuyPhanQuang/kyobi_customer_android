package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetHomepageKeyDataQuery
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TopCatalogStatus
import com.kyobi.domain.model.ShopifyMedia

fun mapTopCatalogs(
    nodes: List<GetHomepageKeyDataQuery.Node>,
    imagesData: List<ShopifyMedia>
): List<TopCatalog> {
    return nodes.mapNotNull { node ->
        node.onMetaobject?.let { metaobject ->
            try {
                val fieldMap = metaobject.fields.associate { it.key to it.value }
                val imageValue = fieldMap["image"]
                val image = imagesData.find { it.id == imageValue }
                TopCatalog(
                    link = fieldMap["link"] ?: "",
                    order = fieldMap["order"]?.toIntOrNull() ?: 0,
                    tag = fieldMap["tag"] ?: "",
                    title = fieldMap["title"] ?: "",
                    image = image,
                    status = fieldMap["status"]?.let {
                        if (it == "active") TopCatalogStatus.ACTIVE else TopCatalogStatus.INACTIVE
                    } ?: TopCatalogStatus.INACTIVE
                )
            } catch (e: Exception) {
                null
            }
        }
    }.filter { it.status == TopCatalogStatus.ACTIVE }
        .sortedBy { it.order }
}