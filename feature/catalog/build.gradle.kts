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
}
android {
    namespace = "com.kyobi.feature.catalog"
}
