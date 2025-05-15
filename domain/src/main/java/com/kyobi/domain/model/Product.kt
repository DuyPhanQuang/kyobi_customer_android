package com.kyobi.domain.model

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
    val selectedOptions: List<SelectedOption>? = emptyList(),
    val price: Money,
    val image: ShopifyImage? = null,
    val quantityAvailable: Int? = null,
    val quantityRule: QuantityRule,
    val taxable: Boolean,
    val compareAtPrice: Money? = null,
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

data class Product(
    val id: String,
    val handle: String,
    val availableForSale: Boolean,
    val title: String,
    val description: String,
    val descriptionHtml: String,
    val options: List<ProductOption>? = emptyList(),
    val priceRange: ProductPriceRange,
    val compareAtPriceRange: ProductPriceRange,
    val variants: List<ProductVariant>? = emptyList(),
    val featuredImage: ShopifyImage? = null,
    val images: List<ShopifyImage>? = emptyList(),
    val metafields: List<ShopifyMetafield>? = emptyList(),
    val seo: SEO,
    val tags: List<String>,
    val updatedAt: String
) {
    companion object {
        fun empty(id: String): Product {
            return Product(
                id = id,
                handle = "",
                availableForSale = false,
                title = "",
                description = "",
                descriptionHtml = "",
                options = emptyList(),
                priceRange = ProductPriceRange(
                    maxVariantPrice = Money(amount = "0.0", currencyCode = ""),
                    minVariantPrice = Money(amount = "0.0", currencyCode = "")
                ),
                compareAtPriceRange = ProductPriceRange(
                    maxVariantPrice = Money(amount = "0.0", currencyCode = ""),
                    minVariantPrice = Money(amount = "0.0", currencyCode = "")
                ),
                variants = emptyList(),
                featuredImage = null,
                images = emptyList(),
                metafields = null,
                seo = SEO(title = "", description = ""),
                tags = emptyList(),
                updatedAt = ""
            )
        }
    }
}