plugins {
    id("plugin.android-common")
}

dependencies {
    CORE
    COMMON_COMPOSABLE
    COMMON_THEME
    DATA
    DOMAIN
}
android {
    namespace = "com.kyobi.featurecommon.product"
}
