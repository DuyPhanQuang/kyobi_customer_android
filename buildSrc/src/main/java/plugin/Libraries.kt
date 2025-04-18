import Version.AndroidXTestVersion
import Version.AppCompat
import Version.ConstraintLayoutCompose
import Version.CoreKtx
import Version.EspressoCore
import Version.GsonVersion
import Version.GuavaAndroid
import Version.HiltAndroidVersion
import Version.HiltNavigationCompose
import Version.JunitExtKtx
import Version.NavigationCompose
import Version.Okhttp3Version
import Version.SplashScreenApi
import Version.TestRunnerVersion
import Version.TimberVersion
import Version.TruthExt

object Version {
    const val CoreKtx = "1.13.0"
    const val AppCompat = "1.7.0"
    const val AndroidXCompose = "1.7.0"
    const val ComposeMaterial3Components = "1.3.0"
    const val ComposeMaterialComponents = "1.12.0"
    const val ComposeBom = "2024.12.01"
    const val AndroidXLiveData = "1.7.0"
    const val AndroidXLifeCycle = "2.8.0"
    const val NavigationCompose = "2.8.0"
    const val AndroidXTestVersion = "1.6.1"
    const val EspressoCore = "3.6.1"
    const val TestRunnerVersion = "1.6.2"
    const val JunitExtKtx = "1.2.1"
    const val TruthExt = "1.6.0"
    const val Coil = "2.7.0"
    const val HiltNavigationCompose = "1.1.0"
    const val HiltAndroidVersion = "2.51.1"
    const val Accompanist = "0.34.0"
    const val SplashScreenApi = "1.1.0"
    const val ConstraintLayoutCompose = "1.1.0"
    const val GsonVersion = "2.11.0"
    const val GuavaAndroid = "33.2.0-android"
    const val TimberVersion = "5.0.1"
    const val Okhttp3Version = "4.12.0"
    const val Retrofit2Version = "2.11.0"
    const val AndroidXActivity = "1.9.2"
    const val KotlinxCoroutines = "1.10.1"
    const val KotlinReflect = "2.1.10"
    const val LottieVersion = "6.5.0"
    const val RoomVersion = "2.6.1"
    const val Apollo = "3.8.4"
    const val Moshi = "1.15.1"
    const val ConverterMoshi = "2.11.0"
    const val SupabaseAuth = "3.1.4"
}

object Libraries {
    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:$CoreKtx"
        const val appCompat = "androidx.appcompat:appcompat:$AppCompat"
        const val lifecycleRunTimeKtx =
            "androidx.lifecycle:lifecycle-runtime-ktx:${Version.AndroidXLifeCycle}"
        const val lifecycleRunTimeCompose =
            "androidx.lifecycle:lifecycle-runtime-compose:${Version.AndroidXLifeCycle}"
        const val viewModelCompose =
            "androidx.lifecycle:lifecycle-viewmodel-compose:${Version.AndroidXLifeCycle}"
        const val viewModelKtx =
            "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.AndroidXLifeCycle}"
        const val liveData = "androidx.compose.runtime:runtime-livedata:${Version.AndroidXLiveData}"
        const val workManager = "androidx.work:work-runtime-ktx:2.8.1"
        const val splashScreen = "androidx.core:core-splashscreen:$SplashScreenApi"
        const val multiDex = "androidx.multidex:multidex:2.0.1"
        const val security = "androidx.security:security-crypto:1.1.0-alpha06"
    }

    object Compose {
        const val composeBom = "androidx.compose:compose-bom:${Version.ComposeBom}"
        const val composeUi = "androidx.compose.ui:ui:${Version.AndroidXCompose}"
        const val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview:${Version.AndroidXCompose}"
        const val composeMaterial = "com.google.android.material:material:${Version.ComposeMaterialComponents}"
        const val composeMaterial3 = "androidx.compose.material3:material3:${Version.ComposeMaterial3Components}"
        const val composeFoundation = "androidx.compose.foundation:foundation:${Version.AndroidXCompose}"
        const val composeRuntime = "androidx.compose.runtime:runtime:${Version.AndroidXCompose}"
        const val composeActivity = "androidx.activity:activity-compose:${Version.AndroidXActivity}"
        const val composeUiUtil = "androidx.compose.ui:ui-util:${Version.AndroidXCompose}"
        const val constraintLayoutCompose =
            "androidx.constraintlayout:constraintlayout-compose:$ConstraintLayoutCompose"
    }

    object Google {
        const val gson = "com.google.code.gson:gson:$GsonVersion"
        const val guava = "com.google.guava:guava:$GuavaAndroid"
        object Firebase {
            const val bom = "com.google.firebase:firebase-bom:33.0.0"
            const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        }
        object PlayServices {
            const val location = "com.google.android.gms:play-services-location:21.3.0"
            const val map = "com.google.android.gms:play-services-maps:19.0.0"
        }
        const val maps = "com.google.maps.android:maps-compose:3.1.0"
        const val places = "com.google.android.libraries.places:places:3.5.0"
    }

    object Room {
        const val runtime = "androidx.room:room-runtime:${Version.RoomVersion}"
        const val ktx = "androidx.room:room-ktx:${Version.RoomVersion}"
        const val compiler = "androidx.room:room-compiler:${Version.RoomVersion}"
    }

    object Timber {
        const val timber = "com.jakewharton.timber:timber:$TimberVersion"
    }

    object Lottie {
        const val lottie = "com.airbnb.android:lottie-compose:${Version.LottieVersion}"
    }

    object SquareUp {
        const val okhttp3 = "com.squareup.okhttp3:okhttp:$Okhttp3Version"
        const val okhttp3LoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$Okhttp3Version"
        const val retrofit2 = "com.squareup.retrofit2:retrofit:${Version.Retrofit2Version}"
        const val converterGson = "com.squareup.retrofit2:converter-gson:${Version.Retrofit2Version}"
        const val moshi = "com.squareup.moshi:moshi:${Version.Moshi}"
        const val converterMoshi = "com.squareup.retrofit2:converter-moshi:${Version.ConverterMoshi}"
        const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Version.Moshi}"
    }

    object Apollo {
        const val apollo3 = "com.apollographql.apollo3:apollo-runtime:${Version.Apollo}"
    }

    object Supabase {
        const val auth = "io.github.jan-tennert.supabase:auth-kt:${Version.SupabaseAuth}"
    }

    object Coroutine {
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.KotlinxCoroutines}"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.KotlinxCoroutines}"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Version.KotlinxCoroutines}"
    }

    object KotlinReflect {
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Version.KotlinReflect}"
    }

    object Accompanist {
        const val pager =
            "com.google.accompanist:accompanist-pager:0.34.0"
        const val swiperefresh =
            "com.google.accompanist:accompanist-swiperefresh:${Version.Accompanist}"
        const val indicators =
            "com.google.accompanist:accompanist-pager-indicators:${Version.Accompanist}"
        const val systemuicontroller =
            "com.google.accompanist:accompanist-systemuicontroller:${Version.Accompanist}"
        const val navigationMaterial =
            "com.google.accompanist:accompanist-navigation-material:${Version.Accompanist}"
        const val navigationAnimation =
            "com.google.accompanist:accompanist-navigation-animation:${Version.Accompanist}"
        const val permission =
            "com.google.accompanist:accompanist-permissions:${Version.Accompanist}"
        const val flowLayout =
            "com.google.accompanist:accompanist-flowlayout:${Version.Accompanist}"
    }

    object Navigation {
        const val navigationCompose = "androidx.navigation:navigation-compose:$NavigationCompose"
    }

    object Coil {
        const val coilCompose = "io.coil-kt:coil-compose:${Version.Coil}"
    }

    object Test {
        const val testCoreKtx = "androidx.test:core-ktx:$AndroidXTestVersion"
        const val espressorCore = "androidx.test.espresso:espresso-core:$EspressoCore"
        const val junitExtKtx = "androidx.test.ext:junit-ktx:$JunitExtKtx"
        const val truthExt = "androidx.test.ext:truth:$TruthExt"
        const val runner = "androidx.test:runner:$TestRunnerVersion"
    }

    object Hilt {
        const val hiltAndroid = "com.google.dagger:hilt-android:$HiltAndroidVersion"
        const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:$HiltAndroidVersion"
        const val hiltWork =
            "androidx.hilt:hilt-work:$HiltNavigationCompose"

        //hilt compose
        const val hiltNavigationCompose =
            "androidx.hilt:hilt-navigation-compose:$HiltNavigationCompose"
    }
}
