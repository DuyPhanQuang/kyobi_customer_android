import java.util.Properties

plugins {
    id("plugin.android-common")
}

val properties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
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
    namespace = "com.kyobi.feature.createreel"
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src\\main\\res", "src\\main\\res")
            }
        }
    }

    defaultConfig {
        buildConfigField("String", "IMGLY_LICENSE", properties.getProperty("IMGLY_LICENSE"))
    }
}