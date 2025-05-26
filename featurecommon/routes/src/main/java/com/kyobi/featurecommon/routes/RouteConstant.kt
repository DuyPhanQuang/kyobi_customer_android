package com.kyobi.featurecommon.routes

object RouteConstant {
    const val HOME_TAB = "home-tab"
    const val COLLECTION_TAB = "collection-tab"
    const val TREND_TAB = "trend-tab"
    const val PROFILE_TAB = "profile-tab"
    const val COLLECTION = "collection"
    const val PRODUCT = "product"
    const val EDITOR_VIDEO = "editor-video"
}

object RouteKey {
    object EditorVideo {
        const val SELECT_TYPE = "selectType"
        const val URI = "uri"
        const val USER_ID = "userId"
        const val RECORDING = "recording"
    }
    object Collection {
        const val CATEGORY_ID = "categoryId"
        const val SUB_CATEGORY_ID = "subCategoryId"
    }
    object Product {
        const val ID = "id"
        const val SKU = "sku"
        const val COLOR_OPTION = "color"
        const val SIZE_OPTION = "size"
    }
}