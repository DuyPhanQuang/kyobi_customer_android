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
}
android {
    namespace = "com.kyobi.feature.authentication"
}
