package com.kyobi.data.network.impl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.core.exceptions.ShopifyErrorHandler
import com.kyobi.data.graphql.GetCollectionProductsQuery
import com.kyobi.data.graphql.GetCollectionsLargeQuery
import com.kyobi.data.graphql.GetDynamicMediasByIdsQuery
import com.kyobi.data.graphql.GetFilterSetByCateHandleQuery
import com.kyobi.data.graphql.GetHomepageKeyDataQuery
import com.kyobi.data.graphql.GetMediaImageByIdQuery
import com.kyobi.data.graphql.GetMediaImagesByIdsQuery
import com.kyobi.data.graphql.GetMetaobjectsByIdsForFlashsaleQuery
import com.kyobi.data.graphql.GetMetaobjectsByIdsQuery
import com.kyobi.data.graphql.GetProductRecommendationsQuery
import com.kyobi.data.graphql.GetProductsByIdsQuery
import com.kyobi.data.graphql.GetProductsQuery
import com.kyobi.data.graphql.type.HasMetafieldsIdentifier
import com.kyobi.data.graphql.type.MetaobjectHandleInput
import com.kyobi.data.graphql.type.ProductCollectionSortKeys
import com.kyobi.data.graphql.type.ProductSortKeys
import com.kyobi.data.network.ShopifyApiService
import com.kyobi.data.utils.mapper.mapToBanners
import com.kyobi.data.utils.mapper.mapFlashSaleInfos
import com.kyobi.data.utils.mapper.mapToCateFilter
import com.kyobi.data.utils.mapper.mapToCollectionProducts
import com.kyobi.data.utils.mapper.mapToDynamicMedias
import com.kyobi.data.utils.mapper.mapToMediaImage
import com.kyobi.data.utils.mapper.mapToMediaImages
import com.kyobi.data.utils.mapper.mapToMetaobjects
import com.kyobi.data.utils.mapper.mapToTopCatalogs
import com.kyobi.data.utils.mapper.mapToTrendingResearchs
import com.kyobi.data.utils.mapper.removeEdgesAndNodes
import com.kyobi.data.utils.mapper.reshapeCollection
import com.kyobi.data.utils.mapper.reshapeProduct
import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.CateFilter
import com.kyobi.domain.model.FlashSaleInfo
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.ShopifyCollection
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.model.ShopifyMetaobject
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
            val metafieldIdentifiers = if (includeMetafields) {
                identifiers!!.map {
                    HasMetafieldsIdentifier(
                        namespace = Optional.present(it.namespace),
                        key = it.key)
                }
            } else { emptyList() }
            val response: ApolloResponse<GetProductsByIdsQuery.Data> = apolloClient
                .query(GetProductsByIdsQuery(ids = ids, identifiers = metafieldIdentifiers))
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
                .query(GetHomepageKeyDataQuery(handle = handle, key = key))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.page?.metafield?.references?.nodes ?: return emptyList()
            // Fetch media details
            val imageIds = nodes.mapNotNull { node ->
                node.onMetaobject?.fields?.find { it.key == "image" }?.value
            }.filter { it.isNotEmpty() }
            val imagesData = if (imageIds.isNotEmpty()) {
                try {
                    getMediaImagesByIds(imageIds)
                } catch (e: Exception) {
                    emptyList()
                }
            } else { emptyList() }
            return mapToBanners(nodes, imagesData)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getTopCatalogs(handle: String, key: String): List<TopCatalog> {
        try {
            val response: ApolloResponse<GetHomepageKeyDataQuery.Data> = apolloClient
                .query(GetHomepageKeyDataQuery(handle = handle, key = key))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.page?.metafield?.references?.nodes ?: return emptyList()
            // Fetch media details
            val imageIds = nodes.mapNotNull { node ->
                node.onMetaobject?.fields?.find { it.key == "image" }?.value
            }.filter { it.isNotEmpty() }
            val imagesData = if (imageIds.isNotEmpty()) {
                try {
                    getMediaImagesByIds(imageIds)
                } catch (e: Exception) {
                    emptyList()
                }
            } else { emptyList() }
            return mapToTopCatalogs(nodes, imagesData)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getTrendingResearchs(handle: String, key: String): List<TrendingResearch> {
        try {
            val response: ApolloResponse<GetHomepageKeyDataQuery.Data> = apolloClient
                .query(GetHomepageKeyDataQuery(handle = handle, key = key))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.page?.metafield?.references?.nodes ?: return emptyList()
            // Fetch media details
            val imageIds = nodes.mapNotNull { node ->
                node.onMetaobject?.fields?.find { it.key == "thumbnail" }?.value
            }.filter { it.isNotEmpty() }
            val imagesData = if (imageIds.isNotEmpty()) {
                try {
                    getMediaImagesByIds(imageIds)
                } catch (e: Exception) {
                    emptyList()
                }
            } else { emptyList() }
            return mapToTrendingResearchs(nodes, imagesData)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getMediaImagesByIds(imageIds: List<String>): List<ShopifyMedia> {
        try {
            val response: ApolloResponse<GetMediaImagesByIdsQuery.Data> = apolloClient
                .query(GetMediaImagesByIdsQuery(ids = imageIds))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.nodes ?: return emptyList()
            return mapToMediaImages(nodes)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getDynamicMediasByIds(mediaIds: List<String>): List<ShopifyMedia> {
        try {
            val response: ApolloResponse<GetDynamicMediasByIdsQuery.Data> = apolloClient
                .query(GetDynamicMediasByIdsQuery(ids = mediaIds))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.nodes ?: return emptyList()
            return mapToDynamicMedias(nodes)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getMediaImage(imageId: String): ShopifyMedia? {
        try {
            val response: ApolloResponse<GetMediaImageByIdQuery.Data> = apolloClient
                .query(GetMediaImageByIdQuery(id = imageId))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val node = response.data?.node ?: return null
            return mapToMediaImage(node.onMediaImage)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getFlashSaleInfosByMetaobjectIds(metaobjectIds: List<String>): List<FlashSaleInfo> {
        try {
            val response: ApolloResponse<GetMetaobjectsByIdsForFlashsaleQuery.Data> = apolloClient
                .query(GetMetaobjectsByIdsForFlashsaleQuery(ids = metaobjectIds))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.nodes ?: return emptyList()
            return mapFlashSaleInfos(nodes)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getMetaobjectsByIds(metaobjectIds: List<String>): List<ShopifyMetaobject> {
        try {
            val response = apolloClient
                .query(GetMetaobjectsByIdsQuery(ids = metaobjectIds))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val nodes = response.data?.nodes ?: return emptyList()
            return mapToMetaobjects(nodes)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getCollectionsLarge(
        handle: String,
        identifiers: List<MetafieldIdentifierRequest>?
    ): ShopifyCollection {
        try {
            val includeMetafields = !identifiers.isNullOrEmpty()
            val metafieldIdentifiers = if (includeMetafields) {
                identifiers!!.map {
                    HasMetafieldsIdentifier(
                        namespace = Optional.present(it.namespace),
                        key = it.key)
                }
            } else { emptyList() }
            val response: ApolloResponse<GetCollectionsLargeQuery.Data> = apolloClient
                .query(GetCollectionsLargeQuery(handle = handle, identifiers = metafieldIdentifiers))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val collection = response.data?.collection
                ?: throw ShopifyApiException(
                    message = "Collection not found for handle: $handle",
                    errorCode = null)
            return reshapeCollection(collection)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getCollectionProducts(
        handle: String,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): ShopifyCollection {
        try {
            val productSortKey = sortKey?.let {
                ProductCollectionSortKeys.valueOf(it.uppercase())
            }
            val effectiveFirst = first ?: 250
            val includeMetafields = !identifiers.isNullOrEmpty()
            val metafieldIdentifiers = if (includeMetafields) {
                identifiers!!.map {
                    HasMetafieldsIdentifier(
                        namespace = Optional.present(it.namespace),
                        key = it.key)
                }
            } else { emptyList() }
            val response: ApolloResponse<GetCollectionProductsQuery.Data> = apolloClient
                .query(
                    GetCollectionProductsQuery(
                        handle = handle,
                        sortKey = Optional.presentIfNotNull(productSortKey),
                        reverse = Optional.presentIfNotNull(reverse),
                        first = Optional.present(effectiveFirst),
                        identifiers = metafieldIdentifiers))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val collection = response.data?.collection
                ?: throw ShopifyApiException(
                    message = "Collection not found for handle: $handle",
                    errorCode = null)
            return mapToCollectionProducts(collection)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getFilterSetByCateHandle(handle: String): CateFilter? {
        try {
            val configFilterType = "filter_set"
            val response: ApolloResponse<GetFilterSetByCateHandleQuery.Data> = apolloClient
                .query(GetFilterSetByCateHandleQuery(
                    MetaobjectHandleInput(handle = handle, type = configFilterType)))
                .execute()
            if (response.hasErrors()) {
                throw ShopifyApiException(
                    message = response.errors?.joinToString { it.message } ?: "Unknown GraphQL error",
                    errorCode = null)
            }
            val metaobject = response.data?.metaobject ?: return null
            return mapToCateFilter(metaobject)
        } catch (e: ApolloException) {
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }
}