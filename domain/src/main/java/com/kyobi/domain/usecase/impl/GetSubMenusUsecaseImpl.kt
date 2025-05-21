package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.core.extensions.toStringListFromJson
import com.kyobi.domain.model.CategoryMenu
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.ShopifyMetaobject
import com.kyobi.domain.model.ShopifyMetaobjectField
import com.kyobi.domain.model.SubCategoryGroup
import com.kyobi.domain.model.SubcategoryMenu
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import com.kyobi.domain.repository.CollectionRepository
import com.kyobi.domain.repository.MetaobjectRepository
import com.kyobi.domain.usecase.GetSubMenusUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class GetSubMenusUseCaseImpl @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val metaobjectRepository: MetaobjectRepository
): GetSubMenusUseCase {
    override suspend fun getSubMenus(
        handle: String
    ): Flow<DomainNetworkResult<List<CategoryMenu>>>  {
       return flow {
           emit(DomainNetworkResult.Loading)
          try {
              // Step1: get collection với metafields
              val identifiers = listOf(
                  MetafieldIdentifierRequest(namespace = "kyobi", key = "collection_category")
              )
              val collection = collectionRepository.getCollectionsLarge(
                  handle = handle,
                  identifiers = identifiers)
              val categoryMetafield = collection.metafields?.firstOrNull()
                  ?: throw ShopifyApiException(
                      message = "Category metafield not found for collection: $handle",
                      errorCode = null)
              // Step2: get categories từ references
              val categories = categoryMetafield.references?.nodes
                  ?: throw ShopifyApiException(
                      message = "No category references found in metafield",
                      errorCode = null)
              // Step3: get groupedSubCategoryIds
              val groupedSubCategoryIds = categories.flatMap { node ->
                  val field = node.fields?.find { it.key == "groupedSubCategories" }
                  field?.value?.toStringListFromJson() ?: emptyList()
              }
              if (groupedSubCategoryIds.isEmpty()) {
                  emit(DomainNetworkResult.Success(emptyList()))
                  return@flow
              }
              // Step4: get subcategoryGroups
              val subcategoryGroups = metaobjectRepository.getMetaobjectsByIds(groupedSubCategoryIds)
                  .ifEmpty {
                      throw ShopifyApiException(
                          message = "No subcategory groups found for IDs: $groupedSubCategoryIds",
                          errorCode = null)
                  }
              // Step5: Lấy subCategoryIds
              val subCategoryIds = subcategoryGroups.flatMap { node ->
                  val field = node.fields?.find { it.key == "subcategories" }
                  field?.value?.toStringListFromJson() ?: emptyList()
              }
              if (subCategoryIds.isEmpty()) {
                  emit(DomainNetworkResult.Success(emptyList()))
                  return@flow
              }
              // Step6: Lấy subcategories
              val subcategories = metaobjectRepository.getMetaobjectsByIds(subCategoryIds)
                  .ifEmpty {
                      throw ShopifyApiException(
                          message = "No subcategories found for IDs: $subCategoryIds",
                          errorCode = null)
                  }
              // Step7: Map List<CategoryMenu>
              val metaobjectsData = mappedMetaobjectsData(categories, subcategoryGroups, subcategories)
              val menus = metaobjectsData.sortedBy { it.order }
              emit(DomainNetworkResult.Success(menus))
          } catch (e: ShopifyApiException) {
              emit(DomainNetworkResult.Error.ShopifyApi(e))
          } catch (e: Exception) {
              emit(DomainNetworkResult.Error.Generic(e))
          }
       } .catch { throwable ->
           emit(DomainNetworkResult.Error.Generic(throwable))
       }
    }

    private fun mappedMetaobjectsData(
        categories: List<ShopifyMetaobject>,
        subcategoryGroups: List<ShopifyMetaobject>,
        subcategories: List<ShopifyMetaobject>
    ): List<CategoryMenu> {
        val subcategoryMap = subcategories.associateBy { it.id }
        return categories.map { category ->
            val flattenCategoryFields = category.fields?.let { flattenFields(it) } ?: emptyMap()
            val validGroupIds = flattenCategoryFields["groupedSubCategories"]
                ?.let { (it as String).toStringListFromJson() } ?: emptyList()
            val groups = subcategoryGroups
                .filter { validGroupIds.contains(it.id) }
                .map { group ->
                    val subcategoriesInGroup = getSubcategoriesForGroup(group, subcategoryMap)
                    val flattenGroupFields = group.fields?.let { flattenFields(it) } ?: emptyMap()
                    SubCategoryGroup(
                        id = group.id,
                        handle = group.handle,
                        groupInfo = flattenGroupFields["group_info"] as? String ?: "",
                        type = flattenGroupFields["group_type"] as? String ?: "",
                        label = flattenGroupFields["label"] as? String ?: "",
                        order = (flattenGroupFields["order"] as? String)?.toIntOrNull() ?: 0,
                        subcategories = subcategoriesInGroup
                    )
                }
                .sortedBy { it.order }
            CategoryMenu(
                id = category.id,
                handle = category.handle,
                filterHandle = flattenCategoryFields["filter_handle"] as? String ?: "",
                title = flattenCategoryFields["label"] as? String ?: "",
                order = (flattenCategoryFields["order"] as? String)?.toIntOrNull() ?: 0,
                thumbnail = null,
                thumbnailInfo = null,
                groups = groups
            )
        }.sortedBy { it.order }
    }

    private fun getSubcategoriesForGroup(
        group: ShopifyMetaobject,
        subcategoryMap: Map<String, ShopifyMetaobject>
    ): List<SubcategoryMenu> {
        val flattenGroupFields = group.fields?.let { flattenFields(it) } ?: emptyMap()
        val validSubCategoryIds = flattenGroupFields["subcategories"]
            ?.let { (it as String).toStringListFromJson() } ?: emptyList()
        return validSubCategoryIds
            .mapNotNull { subcategoryMap[it] }
            .map { subcategory ->
                val flattenSubcategoryFields = subcategory.fields?.let { flattenFields(it) } ?: emptyMap()
                SubcategoryMenu(
                    id = subcategory.id,
                    handle = subcategory.handle,
                    filterHandle = flattenSubcategoryFields["filter_handle"] as? String ?: "",
                    title = flattenSubcategoryFields["label"] as? String ?: "",
                    thumbnail = flattenSubcategoryFields["thumbnail"] as? String,
                    thumbnailInfo = null
                )
            }
    }

    private fun flattenFields(fields: List<ShopifyMetaobjectField>): Map<String, Any?> {
        return fields.associate { it.key to it.value }
    }
}