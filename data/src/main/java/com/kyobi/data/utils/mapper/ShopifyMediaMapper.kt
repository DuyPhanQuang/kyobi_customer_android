package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetDynamicMediasByIdsQuery
import com.kyobi.data.graphql.GetMediaImageByIdQuery
import com.kyobi.data.graphql.GetMediaImagesByIdsQuery
import com.kyobi.domain.model.ShopifyImage
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.model.ShopifySource
import com.kyobi.domain.model.ShopifyVideoPreviewImage

fun mapToMediaImages(nodes: List<GetMediaImagesByIdsQuery.Node?>): List<ShopifyMedia> {
    return nodes.mapNotNull { node ->
        node?.onMediaImage?.let { media ->
            ShopifyMedia(
                id = media.id,
                image = media.image?.let {
                    ShopifyImage(
                        url = it.url.toString(),
                        altText = it.altText,
                        width = it.width?.toFloat(),
                        height = it.height?.toFloat()
                    )
                },
                previewImage = null,
                sources = emptyList()
            )
        }
    }
}

fun mapToDynamicMedias(nodes: List<GetDynamicMediasByIdsQuery.Node?>): List<ShopifyMedia> {
    return nodes.mapNotNull { node ->
        when {
            node?.onMediaImage != null -> ShopifyMedia(
                id = node.onMediaImage.id,
                image = node.onMediaImage.image?.let {
                    ShopifyImage(
                        url = it.url.toString(),
                        altText = it.altText,
                        width = it.width?.toFloat(),
                        height = it.height?.toFloat()
                    )
                },
                previewImage = null,
                sources = emptyList()
            )
            node?.onVideo != null -> ShopifyMedia(
                id = node.onVideo.id,
                image = null,
                previewImage = node.onVideo.previewImage?.let {
                    ShopifyVideoPreviewImage(
                        url = it.url.toString()
                    )
                },
                sources = node.onVideo.sources.map { source ->
                    ShopifySource(
                        url = source.url,
                        format = source.format
                    )
                }
            )
            else -> null
        }
    }
}

fun mapToMediaImage(node: GetMediaImageByIdQuery.OnMediaImage?): ShopifyMedia? {
    return node?.let { media ->
        ShopifyMedia(
            id = media.id,
            image = media.image?.let {
                ShopifyImage(
                    url = it.url.toString(),
                    altText = it.altText,
                    width = it.width?.toFloat(),
                    height = it.height?.toFloat()
                )
            },
            previewImage = null,
            sources = emptyList()
        )
    }
}