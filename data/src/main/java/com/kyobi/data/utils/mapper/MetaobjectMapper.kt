package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetMetaobjectsByIdsQuery
import com.kyobi.domain.model.ShopifyMetaobject
import com.kyobi.domain.model.ShopifyMetaobjectField

fun mapToMetaobjects(nodes: List<GetMetaobjectsByIdsQuery.Node?>): List<ShopifyMetaobject> {
    return nodes.mapNotNull { node ->
        node?.onMetaobject?.let { metaobject ->
            ShopifyMetaobject(
                id = metaobject.id,
                handle = metaobject.handle,
                type = metaobject.type,
                fields = metaobject.fields.map { field ->
                    ShopifyMetaobjectField(
                        key = field.key,
                        value = field.value
                    )
                }
            )
        }
    }
}