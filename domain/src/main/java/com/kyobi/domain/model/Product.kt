package com.kyobi.domain.model

data class Product(
    val id: String,
    val handle: String,
    val availableForSale: Boolean,
    val title: String,
    val description: String,
    val descriptionHtml: String,
    val options: List<ProductOption>,
    val priceRange: ProductPriceRange,
    val compareAtPriceRange: ProductPriceRange,
    val variants: List<ProductVariant>,
    val featuredImage: ShopifyImage? = null,
    val images: List<ShopifyImage>,
    val metafields: List<ShopifyProductMetafield>? = null,
    val seo: SEO,
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
    val sku: String? = null,
    val barcode: String? = null,
    val title: String,
    val availableForSale: Boolean,
    val selectedOptions: List<SelectedOption>? = null,
    val price: Money,
    val image: ShopifyImage? = null,
    val quantityAvailable: Int? = null,
    val quantityRule: QuantityRule,
    val taxable: Boolean,
    val compareAtPrice: Money? = null,
)

data class ShopifyProductMetafield(
    val id: String,
    val type: String,
    val key: String,
    val value: String,
    val references: ShopifyReferences? = null,
)

data class SelectedOption(
    val name: String,
    val value: String
)

data class QuantityRule(
    val increment: Int,
    val minimum: Int,
    val maximum: Int? = null
)