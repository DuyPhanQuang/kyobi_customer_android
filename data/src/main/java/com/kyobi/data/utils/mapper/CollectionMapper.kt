package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetCollectionProductsQuery
import com.kyobi.data.graphql.GetCollectionsLargeQuery
import com.kyobi.domain.model.PageInfo
import com.kyobi.domain.model.SEO
import com.kyobi.domain.model.ShopifyCollection
import com.kyobi.domain.model.ShopifyMetafield
import com.kyobi.domain.model.ShopifyMetaobject
import com.kyobi.domain.model.ShopifyMetaobjectField
import com.kyobi.domain.model.ShopifyReferences

fun reshapeCollection(collection: GetCollectionsLargeQuery.Collection): ShopifyCollection {
    return ShopifyCollection(
        handle = collection.handle,
        title = collection.title,
        description = collection.description,
        seo = SEO(
            description = collection.seo.description,
            title = collection.seo.title
        ),
        updatedAt = collection.updatedAt.toString(),
        metafields = collection.metafields.mapNotNull { metafield ->
            metafield?.let {
                ShopifyMetafield(
                    id = it.id,
                    type = it.type,
                    key = it.key,
                    value = it.value,
                    references = it.references?.nodes?.mapNotNull { node ->
                        node.onMetaobject?.let { metaobject ->
                            ShopifyMetaobject(
                                id = metaobject.id,
                                handle = metaobject.handle,
                                fields = metaobject.fields.map { field ->
                                    field.let { f ->
                                        ShopifyMetaobjectField(
                                            key = f.key,
                                            value = f.value
                                        )
                                    }
                                }
                            )
                        }
                    }?.let { ShopifyReferences(nodes = it) }
                )
            }
        },
        products = emptyList(),
        pageInfo = null
    )
}

fun mapToCollectionProducts(collection: GetCollectionProductsQuery.Collection): ShopifyCollection {
    return collection.metafields.mapNotNull { metafield ->
        metafield?.let {
            ShopifyMetafield(
                id = it.id,
                type = it.type,
                key = it.key,
                value = it.value,
                references = null
            )
        }
    }.let {
        ShopifyCollection(
            id = collection.id,
            title = collection.title,
            metafields = it,
            products = collection.products.edges.mapNotNull { edge ->
                reshapeProduct(edge.node)
            },
            pageInfo = PageInfo(
                hasNextPage = collection.products.pageInfo.hasNextPage,
                endCursor = collection.products.pageInfo.endCursor
            )
        )
    }
}