package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetMetaobjectsByIdsForFlashsaleQuery
import com.kyobi.domain.model.FlashSaleInfo
import com.kyobi.domain.model.ShopifyMedia
import org.json.JSONArray
import org.json.JSONException

fun mapFlashSaleInfos(
    nodes: List<GetMetaobjectsByIdsForFlashsaleQuery.Node?>,
    mediaData: List<ShopifyMedia>
): List<FlashSaleInfo> {
    return nodes.mapNotNull { node ->
        node?.onMetaobject?.let { metaobject ->
            try {
                val fieldMap = metaobject.fields.associate { it.key to it.value }
                val backgroundValue = fieldMap["background"]
                val background = mediaData.find { it.id == backgroundValue }?.image
                val productIds = try {
                    fieldMap["products"]?.let { json ->
                        val jsonArray = JSONArray(json)
                        (0 until jsonArray.length()).map { jsonArray.getString(it) }
                    } ?: emptyList()
                } catch (e: JSONException) {
                    emptyList()
                }
                FlashSaleInfo(
                    id = metaobject.id,
                    handle = metaobject.handle,
                    type = metaobject.type,
                    name = fieldMap["name"],
                    startTime = fieldMap["start_time"],
                    endTime = fieldMap["end_time"],
                    background = background,
                    productIds = productIds
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}