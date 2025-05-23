package com.kyobi.data.utils.mapper

import com.kyobi.core.extensions.toGids
import com.kyobi.data.graphql.GetFilterSetByCateHandleQuery
import com.kyobi.domain.model.CateFilter
import com.kyobi.domain.model.CateFilterMetaobjectField
import com.kyobi.domain.model.ShopifyCateMetaobjectField
import com.kyobi.domain.model.ShopifyMetaobject
import com.kyobi.domain.model.ShopifyMetaobjectField
import com.kyobi.domain.model.ShopifyReferences

fun reshapeCateFilterMetaobjectField(fields: List<ShopifyCateMetaobjectField>): List<CateFilterMetaobjectField> {
    return fields.map { field ->
        val label = field.key
            .replace("filter-", "")
            .let { str -> str.first().uppercase() + str.drop(1) }
        val namespaceKey = field.key.replace("filter-", "")

        CateFilterMetaobjectField(
            label = label,
            key = namespaceKey,
            originalKey = field.key,
            ids = field.value.toGids(),
            selectedIds = emptyList(),
            references = field.references
        )
    }
}

fun mapToCateFilter(data: GetFilterSetByCateHandleQuery.Metaobject): CateFilter {
    return CateFilter(
        filterSetId = data.id,
        cateHandle = data.handle,
        fields = reshapeCateFilterMetaobjectField(data.fields.map { field ->
            ShopifyCateMetaobjectField(
                key = field.key,
                value = field.value,
                type = field.type,
                references = field.references?.let { refs ->
                    ShopifyReferences(
                        refs.nodes.mapNotNull { node ->
                            node.onMetaobject?.let { metaobject ->
                                ShopifyMetaobject(
                                    id = metaobject.id,
                                    handle = metaobject.handle,
                                    fields = metaobject.fields.map { f ->
                                        ShopifyMetaobjectField(
                                            key = f.key,
                                            value = f.value
                                        )
                                    }
                                )
                            }
                        }
                    )
                } ?: ShopifyReferences(emptyList())
            )
        })
    )
}