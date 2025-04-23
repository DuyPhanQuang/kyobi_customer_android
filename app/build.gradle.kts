import plugin.AppConfig
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val properties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}
val devKeystoreProperties = Properties().apply {
    load(File(rootDir, "app/keystore/keystore-dev.properties").inputStream())
}
val prodKeystoreProperties = Properties().apply {
    load(File(rootDir, "app/keystore/keystore-prod.properties").inputStream())
}

android {
    namespace = "com.kyobi.customer"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        applicationId = "com.kyobi.customer"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = AppConfig.versionCode
        versionName = AppConfig.VersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            // Filter for architectures supported by Flutter
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }

        // build config
        buildConfigField("String", "BASE_URL", properties.getProperty("BASE_URL"))
        buildConfigField("String", "SHOPIFY_BASE_URL", properties.getProperty("SHOPIFY_BASE_URL"))
        buildConfigField("String", "SHOPIFY_API_VERSION", properties.getProperty("SHOPIFY_API_VERSION"))
        buildConfigField("String", "X_SHOPIFY_STOREFRONT_ACCESS_TOKEN", properties.getProperty("X_SHOPIFY_STOREFRONT_ACCESS_TOKEN"))
    }

    signingConfigs {
        create("devSigning") {
            storeFile = file(devKeystoreProperties["storeFile"] as String)
            storePassword = devKeystoreProperties["storePassword"] as String
            keyAlias = devKeystoreProperties["keyAlias"] as String
            keyPassword = devKeystoreProperties["keyPassword"] as String
        }

        create("prodSigning") {
            storeFile = file(prodKeystoreProperties["storeFile"] as String)
            storePassword = prodKeystoreProperties["storePassword"] as String
            keyAlias = prodKeystoreProperties["keyAlias"] as String
            keyPassword = prodKeystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            initWith(getByName("debug"))
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "retrofit2.pro",
                "coroutines.pro",
                "okhttp3.pro",
            )
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            initWith(getByName("release"))
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "retrofit2.pro",
                "coroutines.pro",
                "okhttp3.pro",
            )
        }

        maybeCreate("profile").apply {
            initWith(getByName("debug"))
            isDebuggable = false
            isMinifyEnabled = true
        }
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            // Signed
            signingConfig = signingConfigs.getByName("devSigning")
        }

        create("prod") {
            dimension = "env"

            // Signed
            signingConfig = signingConfigs.getByName("prodSigning")
        }
    }

    configurations {
        getByName("profileImplementation") {
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs.toMutableList().apply {
            add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging  {
        resources.excludes.add("META-INF/**/*")
    }
}

dependencies {
    baseDependencies()
    viewDependencies()
    composeDependencies()
    moduleDependencies()
    testDependencies()

    implementation(kotlin("stdlib"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}