package com.kyobi.domain.model

data class Product(
    val id: String,
    val handle: String,
    val availableForSale: Boolean,
    val title: String,
    val description: String,
    val options: List<ProductOption>,
    val priceRange: ProductPriceRange,
    val compareAtPriceRange: ProductPriceRange,
    val variants: List<ProductVariant>,
    val featuredImage: ProductImage?,
    val images: List<ProductImage>,
    val tags: List<String>,
    val updatedAt: String
)

data class ProductOption(
    val id: String,
    val name: String,
    val values: List<String>
)

data class ProductPriceRange(
    val maxVariantPrice: Money,
    val minVariantPrice: Money
)

data class Money(
    val amount: String,
    val currencyCode: String
)

data class ProductVariant(
    val id: String,
    val sku: String?,
    val title: String,
    val availableForSale: Boolean,
    val price: Money,
    val image: ProductImage?,
    val quantityAvailable: Int?
)

data class ProductImage(
    val url: String,
    val altText: String?,
    val width: Int?,
    val height: Int?
)