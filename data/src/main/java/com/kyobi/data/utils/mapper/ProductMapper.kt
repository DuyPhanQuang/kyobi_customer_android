package com.kyobi.data.utils.mapper

import com.kyobi.data.graphql.GetProductRecommendationsQuery
import com.kyobi.data.graphql.GetProductsByIdsQuery
import com.kyobi.data.graphql.GetProductsQuery
import com.kyobi.domain.model.Money
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.ProductOption
import com.kyobi.domain.model.ProductPriceRange
import com.kyobi.domain.model.ProductVariant
import com.kyobi.domain.model.QuantityRule
import com.kyobi.domain.model.SEO
import com.kyobi.domain.model.SelectedOption
import com.kyobi.domain.model.ShopifyImage
import com.kyobi.domain.model.ShopifyMetaobject
import com.kyobi.domain.model.ShopifyMetaobjectField
import com.kyobi.domain.model.ShopifyProductMetafield
import com.kyobi.domain.model.ShopifyReferences

fun removeEdgesAndNodes(products: GetProductsQuery.Products): List<GetProductsQuery.Node> {
    return products.edges.map { edge -> edge.node }
}

fun reshapeProduct(node: GetProductsQuery.Node): Product? {
    return try {
        Product(
            id = node.id,
            handle = node.handle,
            availableForSale = node.availableForSale,
            title = node.title,
            description = node.description,
            descriptionHtml = node.descriptionHtml.toString(),
            options = node.options.map { option ->
                ProductOption(
                    id = option.id,
                    name = option.name,
                    values = option.values
                )
            },
            priceRange = ProductPriceRange(
                maxVariantPrice = Money(
                    amount = node.priceRange.maxVariantPrice.amount.toString(),
                    currencyCode = node.priceRange.maxVariantPrice.currencyCode.rawValue
                ),
                minVariantPrice = Money(
                    amount = node.priceRange.minVariantPrice.amount.toString(),
                    currencyCode = node.priceRange.minVariantPrice.currencyCode.rawValue
                )
            ),
            compareAtPriceRange = ProductPriceRange(
                maxVariantPrice = Money(
                    amount = node.compareAtPriceRange.maxVariantPrice.amount.toString(),
                    currencyCode = node.compareAtPriceRange.maxVariantPrice.currencyCode.rawValue
                ),
                minVariantPrice = Money(
                    amount = node.compareAtPriceRange.minVariantPrice.amount.toString(),
                    currencyCode = node.compareAtPriceRange.minVariantPrice.currencyCode.rawValue
                )
            ),
            variants = node.variants.edges.map { edge ->
                edge.node.let { variant ->
                    ProductVariant(
                        id = variant.id,
                        sku = variant.sku,
                        barcode = variant.barcode,
                        title = variant.title,
                        availableForSale = variant.availableForSale,
                        selectedOptions = variant.selectedOptions.map { option ->
                            SelectedOption(
                                name = option.name,
                                value = option.value
                            )
                        },
                        price = Money(
                            amount = variant.price.amount.toString(),
                            currencyCode = variant.price.currencyCode.rawValue
                        ),
                        image = variant.image?.let { img ->
                            ShopifyImage(
                                url = img.url.toString(),
                                altText = img.altText,
                                width = img.width?.toFloat(),
                                height = img.height?.toFloat()
                            )
                        },
                        quantityAvailable = variant.quantityAvailable,
                        quantityRule = QuantityRule(
                            increment = variant.quantityRule.increment,
                            minimum = variant.quantityRule.minimum,
                            maximum = variant.quantityRule.maximum
                        ),
                        taxable = variant.taxable,
                        compareAtPrice = variant.compareAtPrice?.let { price ->
                            Money(
                                amount = price.amount.toString(),
                                currencyCode = price.currencyCode.rawValue
                            )
                        }
                    )
                }
            },
            featuredImage = node.featuredImage?.let { img ->
                ShopifyImage(
                    url = img.url.toString(),
                    altText = img.altText,
                    width = img.width?.toFloat(),
                    height = img.height?.toFloat()
                )
            },
            images = node.images.edges.map { edge ->
                edge.node.let { img ->
                    ShopifyImage(
                        url = img.url.toString(),
                        altText = img.altText,
                        width = img.width?.toFloat(),
                        height = img.height?.toFloat()
                    )
                }
            },
            metafields = node.metafields.mapNotNull { metafield ->
                metafield?.let { meta ->
                    ShopifyProductMetafield(
                        id = meta.id,
                        type = meta.type,
                        key = meta.key,
                        value = meta.value,
                        references = meta.references?.nodes?.mapNotNull { ref ->
                            ref.onMetaobject?.let { metaobject ->
                                ShopifyMetaobject(
                                    id = metaobject.id,
                                    handle = metaobject.handle,
                                    type = metaobject.type,
                                    fields = metaobject.fields.mapNotNull { field ->
                                        field.value?.let { value ->
                                            ShopifyMetaobjectField(
                                                key = field.key,
                                                value = value
                                            )
                                        }
                                    }
                                )
                            }
                        }?.let { nodes -> if (nodes.isNotEmpty()) ShopifyReferences(nodes = nodes) else null }
                    )
                }
            }.takeIf { it.isNotEmpty() },
            seo = SEO(
                title = node.seo.title,
                description = node.seo.description
            ),
            tags = node.tags,
            updatedAt = node.updatedAt.toString()
        )
    } catch (e: Exception) {
        println("Failed to reshape product GetProductsQuery error: ${e.message}")
        null
    }
}

fun reshapeProduct(node: GetProductsByIdsQuery.OnProduct): Product? {
    return try {
        Product(
            id = node.id,
            handle = node.handle,
            availableForSale = node.availableForSale,
            title = node.title,
            description = node.description,
            descriptionHtml = node.descriptionHtml.toString(),
            options = node.options.map { option ->
                ProductOption(
                    id = option.id,
                    name = option.name,
                    values = option.values
                )
            },
            priceRange = ProductPriceRange(
                maxVariantPrice = Money(
                    amount = node.priceRange.maxVariantPrice.amount.toString(),
                    currencyCode = node.priceRange.maxVariantPrice.currencyCode.rawValue
                ),
                minVariantPrice = Money(
                    amount = node.priceRange.minVariantPrice.amount.toString(),
                    currencyCode = node.priceRange.minVariantPrice.currencyCode.rawValue
                )
            ),
            compareAtPriceRange = ProductPriceRange(
                maxVariantPrice = Money(
                    amount = node.compareAtPriceRange.maxVariantPrice.amount.toString(),
                    currencyCode = node.compareAtPriceRange.maxVariantPrice.currencyCode.rawValue
                ),
                minVariantPrice = Money(
                    amount = node.compareAtPriceRange.minVariantPrice.amount.toString(),
                    currencyCode = node.compareAtPriceRange.minVariantPrice.currencyCode.rawValue
                )
            ),
            variants = node.variants.edges.map { edge ->
                edge.node.let { variant ->
                    ProductVariant(
                        id = variant.id,
                        sku = variant.sku,
                        barcode = variant.barcode,
                        title = variant.title,
                        availableForSale = variant.availableForSale,
                        selectedOptions = variant.selectedOptions.map { opt ->
                            SelectedOption(
                                name = opt.name,
                                value = opt.value
                            )
                        },
                        price = Money(
                            amount = variant.price.amount.toString(),
                            currencyCode = variant.price.currencyCode.rawValue
                        ),
                        image = variant.image?.let { img ->
                            ShopifyImage(
                                url = img.url.toString(),
                                altText = img.altText,
                                width = img.width?.toFloat(),
                                height = img.height?.toFloat()
                            )
                        },
                        quantityAvailable = variant.quantityAvailable,
                        quantityRule = QuantityRule(
                            increment = variant.quantityRule.increment,
                            minimum = variant.quantityRule.minimum,
                            maximum = variant.quantityRule.maximum
                        ),
                        taxable = variant.taxable,
                        compareAtPrice = variant.compareAtPrice?.let { price ->
                            Money(
                                amount = price.amount.toString(),
                                currencyCode = price.currencyCode.rawValue
                            )
                        }
                    )
                }
            },
            featuredImage = node.featuredImage?.let { img ->
                ShopifyImage(
                    url = img.url.toString(),
                    altText = img.altText,
                    width = img.width?.toFloat(),
                    height = img.height?.toFloat()
                )
            },
            images = node.images.edges.map { edge ->
                edge.node.let { img ->
                    ShopifyImage(
                        url = img.url.toString(),
                        altText = img.altText,
                        width = img.width?.toFloat(),
                        height = img.height?.toFloat()
                    )
                }
            },
            metafields = node.metafields.mapNotNull { metafield ->
                metafield?.let { meta ->
                    ShopifyProductMetafield(
                        id = meta.id,
                        type = meta.type,
                        key = meta.key,
                        value = meta.value,
                        references = meta.references?.nodes?.mapNotNull { ref ->
                            ref.onMetaobject?.let { metaobject ->
                                ShopifyMetaobject(
                                    id = metaobject.id,
                                    handle = metaobject.handle,
                                    type = metaobject.type,
                                    fields = metaobject.fields.mapNotNull { field ->
                                        field.value?.let { value ->
                                            ShopifyMetaobjectField(
                                                key = field.key,
                                                value = value
                                            )
                                        }
                                    }
                                )
                            }
                        }?.let { nodes -> if (nodes.isNotEmpty()) ShopifyReferences(nodes = nodes) else null }
                    )
                }
            }.takeIf { it.isNotEmpty() },
            seo = SEO(
                title = node.seo.title,
                description = node.seo.description
            ),
            tags = node.tags,
            updatedAt = node.updatedAt.toString()
        )
    } catch (e: Exception) {
        println("Failed to reshape product GetProductsByIdsQuery error: ${e.message}")
        null
    }
}

fun reshapeProduct(node: GetProductRecommendationsQuery.ProductRecommendation): Product? {
    return try {
        Product(
            id = node.id,
            handle = node.handle,
            availableForSale = node.availableForSale,
            title = node.title,
            description = node.description,
            descriptionHtml = node.descriptionHtml.toString(),
            options = node.options.map { option ->
                ProductOption(
                    id = option.id,
                    name = option.name,
                    values = option.values
                )
            },
            priceRange = ProductPriceRange(
                maxVariantPrice = Money(
                    amount = node.priceRange.maxVariantPrice.amount.toString(),
                    currencyCode = node.priceRange.maxVariantPrice.currencyCode.rawValue
                ),
                minVariantPrice = Money(
                    amount = node.priceRange.minVariantPrice.amount.toString(),
                    currencyCode = node.priceRange.minVariantPrice.currencyCode.rawValue
                )
            ),
            compareAtPriceRange = ProductPriceRange(
                maxVariantPrice = Money(
                    amount = node.compareAtPriceRange.maxVariantPrice.amount.toString(),
                    currencyCode = node.compareAtPriceRange.maxVariantPrice.currencyCode.rawValue
                ),
                minVariantPrice = Money(
                    amount = node.compareAtPriceRange.minVariantPrice.amount.toString(),
                    currencyCode = node.compareAtPriceRange.minVariantPrice.currencyCode.rawValue
                )
            ),
            variants = node.variants.edges.map { edge ->
                edge.node.let { variant ->
                    ProductVariant(
                        id = variant.id,
                        sku = variant.sku,
                        barcode = variant.barcode,
                        title = variant.title,
                        availableForSale = variant.availableForSale,
                        selectedOptions = variant.selectedOptions.map { opt ->
                            SelectedOption(
                                name = opt.name,
                                value = opt.value
                            )
                        },
                        price = Money(
                            amount = variant.price.amount.toString(),
                            currencyCode = variant.price.currencyCode.rawValue
                        ),
                        image = variant.image?.let { img ->
                            ShopifyImage(
                                url = img.url.toString(),
                                altText = img.altText,
                                width = img.width?.toFloat(),
                                height = img.height?.toFloat()
                            )
                        },
                        quantityAvailable = variant.quantityAvailable,
                        quantityRule = QuantityRule(
                            increment = variant.quantityRule.increment,
                            minimum = variant.quantityRule.minimum,
                            maximum = variant.quantityRule.maximum
                        ),
                        taxable = variant.taxable,
                        compareAtPrice = variant.compareAtPrice?.let { price ->
                            Money(
                                amount = price.amount.toString(),
                                currencyCode = price.currencyCode.rawValue
                            )
                        }
                    )
                }
            },
            featuredImage = node.featuredImage?.let { img ->
                ShopifyImage(
                    url = img.url.toString(),
                    altText = img.altText,
                    width = img.width?.toFloat(),
                    height = img.height?.toFloat()
                )
            },
            images = node.images.edges.map { edge ->
                edge.node.let { img ->
                    ShopifyImage(
                        url = img.url.toString(),
                        altText = img.altText,
                        width = img.width?.toFloat(),
                        height = img.height?.toFloat()
                    )
                }
            },
            metafields = null,
            seo = SEO(
                title = node.seo.title,
                description = node.seo.description
            ),
            tags = node.tags,
            updatedAt = node.updatedAt.toString()
        )
    } catch (e: Exception) {
        println("Failed to reshape product GetProductRecommendationsQuery, error: ${e.message}")
        null
    }
}
