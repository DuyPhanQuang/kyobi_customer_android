import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")

    // GG PlayServices, firebase, maps
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

val properties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
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

//        buildConfigField("String", "BASE_URL", properties.getProperty("BASE_URL"))
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
                "gson.pro",
                "okhttp3.pro",
                "firebase-crashlytics.pro"
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
                "gson.pro",
                "okhttp3.pro",
                "firebase-crashlytics.pro"
            )
        }

        create("profile") {
            initWith(getByName("debug"))
            isDebuggable = true
            isMinifyEnabled = false
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs.toMutableList().apply {
            add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    buildFeatures {
        compose = true
    }

    packaging  {
        resources.excludes.add("META-INF/**/*")
    }
}

dependencies {
    baseDependencies()
    composeDependencies()
    moduleDependencies()
    testDependencies()

    implementation(kotlin("stdlib"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}