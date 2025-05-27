package com.kyobi.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductOption(
    val id: String,
    val name: String,
    val values: List<String>
): Parcelable

@Parcelize
data class ProductPriceRange(
    val maxVariantPrice: Money,
    val minVariantPrice: Money
): Parcelable

@Parcelize
data class Money(
    val amount: String,
    val currencyCode: String
): Parcelable

@Parcelize
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
): Parcelable

@Parcelize
data class SelectedOption(
    val name: String,
    val value: String
): Parcelable

@Parcelize
data class QuantityRule(
    val increment: Int,
    val minimum: Int,
    val maximum: Int? = null
): Parcelable

@Parcelize
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
) : Parcelable {
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