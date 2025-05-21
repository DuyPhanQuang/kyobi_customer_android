plugins {
    id("plugin.android-common")
}

dependencies {
    CORE
    DATA
    DOMAIN
    COMMON_THEME
    COMMON_COMPOSABLE
    FEATURECOMMON_AUTH
    FEATURECOMMON_PRODUCT
    FEATURECOMMON_ROUTES
}
android {
    namespace = "com.kyobi.feature.collection"
}
