plugins {
    id("plugin.android-common")
}


dependencies {
    CORE
    DATA
    DOMAIN
}
android {
    namespace = "com.kyobi.featurecommon.auth"
}
