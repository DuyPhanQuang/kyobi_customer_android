package com.kyobi.data.network.impl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.core.exceptions.ShopifyErrorHandler
import com.kyobi.data.graphql.GetHomepageKeyDataQuery
import com.kyobi.data.graphql.GetMediaImagesByIdsQuery
import com.kyobi.data.graphql.GetProductRecommendationsQuery
import com.kyobi.data.graphql.GetProductsByIdsQuery
import com.kyobi.data.graphql.GetProductsQuery
import com.kyobi.data.graphql.type.HasMetafieldsIdentifier
import com.kyobi.data.graphql.type.ProductSortKeys
import com.kyobi.data.network.ShopifyApiService
import com.kyobi.data.utils.mapper.mapBanners
import com.kyobi.data.utils.mapper.mapTopCatalogs
import com.kyobi.data.utils.mapper.mapTrendingResearchs
import com.kyobi.data.utils.mapper.removeEdgesAndNodes
import com.kyobi.data.utils.mapper.reshapeProduct
import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.ShopifyImage
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TrendingResearch
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopifyApiServiceImpl @Inject constructor(
    private val apolloClient: ApolloClient,
    private val errorHandler: ShopifyErrorHandler
) : ShopifyApiService {
    override suspend fun getProducts(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): List<Product> {
        try {
            val productSortKey = sortKey?.let {
                ProductSortKeys.valueOf(it.uppercase())
            }
            val effectiveFirst = first ?: 250
            val includeMetafields = !identifiers.isNullOrEmpty()
            val indentifiers = if (includeMetafields) {
                identifiers!!.map {
                    HasMetafieldsIdentifier(
                        namespace = Optional.present(it.namespace),
                        key = it.key)
                }
            } else { emptyList() }
            val response: ApolloResponse<GetProductsQuery.Data> = apolloClient
                .query(
                    GetProductsQuery(
                        first = Optional.present(effectiveFirst),
                        query = Optional.presentIfNotNull(query),
                        reverse = Optional.presentIfNotNull(reverse),
                        sortKey = Optional.presentIfNotNull(productSortKey),
                        identifiers = indentifiers))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val products = response.data?.products?.let { products ->
                removeEdgesAndNodes(products).mapNotNull { node -> reshapeProduct(node) }
            } ?: emptyList()
            return products
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getProductRecommendations(productId: String): List<Product> {
        try {
            val response: ApolloResponse<GetProductRecommendationsQuery.Data> = apolloClient
                .query(GetProductRecommendationsQuery(productId = productId))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val products = response.data?.productRecommendations?.mapNotNull { node ->
                reshapeProduct(node)
            } ?: emptyList()
            return products
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getProductsByIds(
        ids: List<String>,
        identifiers: List<MetafieldIdentifierRequest>?
    ): List<Product> {
        try {
            val includeMetafields = !identifiers.isNullOrEmpty()
            val indentifiers = if (includeMetafields) {
                identifiers!!.map {
                    HasMetafieldsIdentifier(
                        namespace = Optional.present(it.namespace),
                        key = it.key)
                }
            } else { emptyList() }
            val response: ApolloResponse<GetProductsByIdsQuery.Data> = apolloClient
                .query(
                    GetProductsByIdsQuery(
                        ids = ids,
                        identifiers = indentifiers))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val products = response.data?.nodes?.mapNotNull { node ->
                node?.onProduct?.let { productNode -> reshapeProduct(productNode) }
            } ?: emptyList()
            return products
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getBanners(handle: String, key: String): List<Banner> {
        try {
            val response: ApolloResponse<GetHomepageKeyDataQuery.Data> = apolloClient
                .query(
                    GetHomepageKeyDataQuery(
                        handle = handle,
                        key = key))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.page?.metafield?.references?.nodes ?: return emptyList()
            // Fetch media details
            val mediaIds = nodes.mapNotNull { node ->
                node.onMetaobject?.fields?.find { it.key == "image" }?.value
            }.filter { it.isNotEmpty() }
            val mediaData = if (mediaIds.isNotEmpty()) {
                try {
                    getMediaImagesByIds(mediaIds)
                } catch (e: Exception) {
                    emptyList()
                }
            } else { emptyList() }
            return mapBanners(nodes, mediaData)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getTopCatalogs(handle: String, key: String): List<TopCatalog> {
        try {
            val response: ApolloResponse<GetHomepageKeyDataQuery.Data> = apolloClient
                .query(
                    GetHomepageKeyDataQuery(
                        handle = handle,
                        key = key))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.page?.metafield?.references?.nodes ?: return emptyList()
            // Fetch media details
            val mediaIds = nodes.mapNotNull { node ->
                node.onMetaobject?.fields?.find { it.key == "image" }?.value
            }.filter { it.isNotEmpty() }
            val mediaData = if (mediaIds.isNotEmpty()) {
                try {
                    getMediaImagesByIds(mediaIds)
                } catch (e: Exception) {
                    emptyList()
                }
            } else { emptyList() }
            return mapTopCatalogs(nodes, mediaData)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getTrendingResearchs(handle: String, key: String): List<TrendingResearch> {
        try {
            val response: ApolloResponse<GetHomepageKeyDataQuery.Data> = apolloClient
                .query(
                    GetHomepageKeyDataQuery(
                        handle = handle,
                        key = key))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.page?.metafield?.references?.nodes ?: return emptyList()
            // Fetch media details
            val mediaIds = nodes.mapNotNull { node ->
                node.onMetaobject?.fields?.find { it.key == "thumbnail" }?.value
            }.filter { it.isNotEmpty() }
            val mediaData = if (mediaIds.isNotEmpty()) {
                try {
                    getMediaImagesByIds(mediaIds)
                } catch (e: Exception) {
                    emptyList()
                }
            } else { emptyList() }
            return mapTrendingResearchs(nodes, mediaData)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getMediaImagesByIds(mediaIds: List<String>): List<ShopifyMedia> {
        try {
            val response: ApolloResponse<GetMediaImagesByIdsQuery.Data> = apolloClient
                .query(GetMediaImagesByIdsQuery(ids = mediaIds))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null
                )
            }
            val nodes = response.data?.nodes
            return nodes?.mapNotNull { node ->
                node?.onMediaImage?.let { media ->
                    ShopifyMedia(
                        id = media.id,
                        image = media.image?.let {
                            ShopifyImage(
                                url = it.url.toString(),
                                altText = it.altText,
                                width = it.width?.toFloat(),
                                height = it.height?.toFloat()
                            )
                        },
                        previewImage = null,
                        sources = null
                    )
                }
            } ?: emptyList()
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }
}