package com.kyobi.domain.extension

import com.kyobi.domain.model.Product
import java.util.Locale

const val colorKey = "color"
const val sizeKey = "size"

val Product.toColorsOption: List<String>
    get() = options.find { it.name.lowercase(Locale.getDefault()) == colorKey }?.values ?: emptyList()

val Product.toSizesOption: List<String>
    get() = options.find { it.name.lowercase(Locale.getDefault()) == sizeKey }?.values ?: emptyList()

val Product.toFormattedSalePrice: String
    get() = "${priceRange.minVariantPrice.currencyCode} ${priceRange.minVariantPrice.amount}"

val Product.toFormattedOriginalPrice: String
    get() = "${compareAtPriceRange.minVariantPrice.currencyCode} ${compareAtPriceRange.minVariantPrice.amount}"