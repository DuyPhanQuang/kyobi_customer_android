package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetHomepageKeyDataQuery
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.model.TrendingResearch
import org.json.JSONArray
import org.json.JSONException

fun mapTrendingResearchs(
    nodes: List<GetHomepageKeyDataQuery.Node>,
    imagesData: List<ShopifyMedia>
): List<TrendingResearch> {
    return nodes.mapNotNull { node ->
        node.onMetaobject?.let { metaobject ->
            try {
                val fieldMap = metaobject.fields.associate { it.key to it.value }
                val thumbnailValue = fieldMap["thumbnail"]
                val thumbnailImage = imagesData.find { it.id == thumbnailValue }
                TrendingResearch(
                    link = fieldMap["link"] ?: "",
                    label = fieldMap["label"] ?: "",
                    order = fieldMap["order"]?.toIntOrNull() ?: 0,
                    tag = fieldMap["tag"] ?: "",
                    title = fieldMap["title"] ?: "",
                    descriptionHtml = fieldMap["description_html"] ?: "",
                    thumbnail = thumbnailImage,
                    allMedias = emptyList(),
                    hashtag = try {
                        fieldMap["hashtag"]?.let { json ->
                            val jsonArray = JSONArray(json)
                            (0 until jsonArray.length()).map { jsonArray.getString(it) }
                        } ?: emptyList()
                    } catch (e: JSONException) {
                        emptyList()
                    },
                    trendReviewIds = try {
                        fieldMap["trend_reviews"]?.let { json ->
                            val jsonArray = JSONArray(json)
                            (0 until jsonArray.length()).map { jsonArray.getString(it) }
                        } ?: emptyList()
                    } catch (e: JSONException) {
                        emptyList()
                    }
                )
            } catch (e: Exception) {
                null
            }
        }
    }.sortedBy { it.order }
}