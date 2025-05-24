package com.kyobi.feature.collection.extension

import com.kyobi.domain.model.CateFilterMetaobjectField
import com.kyobi.feature.collection.model.FilterOption

const val prefixFilterKey = "filter_set"
const val colorFilterKey = "$prefixFilterKey.color-pattern"
const val sizeFilterKey = "$prefixFilterKey.size"

fun CateFilterMetaobjectField.isMatchedColorKey(): Boolean {
    return this.key == "color" || this.key == "color-pattern"
}

fun CateFilterMetaobjectField.isMatchedSizeKey(): Boolean {
    return this.key == "size"
}

fun List<CateFilterMetaobjectField>?.toColorFilterOptions(): List<FilterOption> {
    if (this == null) return emptyList()
    val data = this.flatMap { field ->
        field.references.nodes?.mapNotNull { node ->
            val labelField = node.fields?.find { it.key == "label" }
            val colorField = node.fields?.find { it.key == "color" }
            labelField?.value?.let { label ->
                FilterOption(
                    label = label,
                    handle = node.handle,
                    code = colorField?.value,
                    key = colorFilterKey
                )
            }
        } ?: emptyList()
    }
    return data
}

fun List<CateFilterMetaobjectField>?.toFilterOptions(): List<FilterOption> {
    if (this == null) return emptyList()
    val data = this.flatMap { field ->
        field.references.nodes?.mapNotNull { node ->
            val labelField = node.fields?.find { it.key == "label" }
            labelField?.value?.let { label ->
                FilterOption(
                    label = label,
                    handle = node.handle,
                    key = sizeFilterKey
                )
            }
        } ?: emptyList()
    }
    return data
}
