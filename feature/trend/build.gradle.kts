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
    FEATURECOMMON_MONITOR
}
android {
    namespace = "com.kyobi.feature.trend"
}