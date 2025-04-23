

object Version {
    const val CoreKtx = "1.16.0"
    const val AppCompat = "1.7.0"
    const val AndroidXCompose = "1.7.0"
    const val ComposeMaterial3Components = "1.3.0"
    const val ComposeMaterialComponents = "1.12.0"
    const val ComposeBom = "2024.12.01"
    const val AndroidXLiveData = "1.7.0"
    const val AndroidXLifeCycle = "2.8.0"
    const val NavigationCompose = "2.8.0"
    const val AndroidXTest = "1.6.1"
    const val EspressoCore = "3.6.1"
    const val TestRunner = "1.6.2"
    const val JunitExtKtx = "1.2.1"
    const val TruthExt = "1.6.0"
    const val Coil = "2.7.0"
    const val HiltNavigationCompose = "1.2.0"
    const val HiltAndroid = "2.56.1"
    const val Accompanist = "0.34.0"
    const val SplashScreenApi = "1.1.0"
    const val ConstraintLayoutCompose = "1.1.0"
    const val Gson = "2.11.0"
    const val GuavaAndroid = "33.2.0-android"
    const val Timber = "5.0.1"
    const val Okhttp3 = "4.12.0"
    const val Retrofit2 = "2.11.0"
    const val AndroidXActivity = "1.9.2"
    const val KotlinxCoroutines = "1.10.1"
    const val KotlinReflect = "2.1.10"
    const val Lottie = "6.5.0"
    const val Room = "2.6.1"
    const val Apollo = "4.0.0-beta.7"
    const val Moshi = "1.15.1"
    const val ConverterMoshi = "2.11.0"
    const val Supabase = "3.1.4"
    const val RecycleView = "1.4.0"
    const val ConstraintLayout = "2.2.1"
    const val MediaPlayer = "1.6.1"
    const val Glide = "4.16.0"
    const val Semver = "5.6.0"
}

object Libraries {
    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:${Version.CoreKtx}"
        const val appCompat = "androidx.appcompat:appcompat:${Version.AppCompat}"
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
        const val splashScreen = "androidx.core:core-splashscreen:${Version.SplashScreenApi}"
        const val multiDex = "androidx.multidex:multidex:2.0.1"
        const val security = "androidx.security:security-crypto:1.1.0-alpha06"
        const val recyclerView = "androidx.recyclerview:recyclerview:${Version.RecycleView}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Version.ConstraintLayout}"
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
            "androidx.constraintlayout:constraintlayout-compose:${Version.ConstraintLayoutCompose}"
    }

    object Google {
        const val gson = "com.google.code.gson:gson:${Version.Gson}"
        const val guava = "com.google.guava:guava:${Version.GuavaAndroid}"
        object Firebase {
            const val bom = "com.google.firebase:firebase-bom:33.0.0"
            const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
            const val analytics = "com.google.firebase:firebase-analytics-ktx"
        }
        object PlayServices {
            const val location = "com.google.android.gms:play-services-location:21.3.0"
            const val map = "com.google.android.gms:play-services-maps:19.0.0"
        }
        const val maps = "com.google.maps.android:maps-compose:3.1.0"
        const val places = "com.google.android.libraries.places:places:3.5.0"
    }

    object Room {
        const val runtime = "androidx.room:room-runtime:${Version.Room}"
        const val ktx = "androidx.room:room-ktx:${Version.Room}"
        const val compiler = "androidx.room:room-compiler:${Version.Room}"
    }

    object Timber {
        const val timber = "com.jakewharton.timber:timber:${Version.Timber}"
    }

    object Lottie {
        const val lottie = "com.airbnb.android:lottie-compose:${Version.Lottie}"
    }

    object SquareUp {
        const val okhttp3 = "com.squareup.okhttp3:okhttp:${Version.Okhttp3}"
        const val okhttp3LoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Version.Okhttp3}"
        const val retrofit2 = "com.squareup.retrofit2:retrofit:${Version.Retrofit2}"
        const val converterGson = "com.squareup.retrofit2:converter-gson:${Version.Retrofit2}"
        const val moshi = "com.squareup.moshi:moshi:${Version.Moshi}"
        const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Version.Moshi}"
        const val converterMoshi = "com.squareup.retrofit2:converter-moshi:${Version.ConverterMoshi}"
        const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Version.Moshi}"
    }

    object Apollo {
        const val apollo3 = "com.apollographql.apollo3:apollo-runtime:${Version.Apollo}"
    }

    object Supabase {
        const val auth = "io.github.jan-tennert.supabase:auth-kt:${Version.Supabase}"
        const val postgrest = "io.github.jan-tennert.supabase:postgrest-kt:${Version.Supabase}"
        const val realtime = "io.github.jan-tennert.supabase:realtime-kt:${Version.Supabase}"
    }

    object Coroutine {
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.KotlinxCoroutines}"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.KotlinxCoroutines}"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Version.KotlinxCoroutines}"
    }

    object KotlinReflect {
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Version.KotlinReflect}"
    }

    object Semver {
        const val semver = "org.semver4j:semver4j:${Version.Semver}"
    }

    object MediaPlayer {
        const val media3ExoPlayer = "androidx.media3:media3-exoplayer:${Version.MediaPlayer}"
        const val media3Ui = "androidx.media3:media3-ui:${Version.MediaPlayer}"
        const val media3ExoPlayerDash = "androidx.media3:media3-exoplayer-dash:${Version.MediaPlayer}"
    }

    object Glide {
        const val glide = "com.github.bumptech.glide:glide:${Version.Glide}"
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
        const val navigationCompose = "androidx.navigation:navigation-compose:${Version.NavigationCompose}"
    }

    object Coil {
        const val coilCompose = "io.coil-kt:coil-compose:${Version.Coil}"
    }

    object Test {
        const val testCoreKtx = "androidx.test:core-ktx:${Version.AndroidXTest}"
        const val espressorCore = "androidx.test.espresso:espresso-core:${Version.EspressoCore}"
        const val junitExtKtx = "androidx.test.ext:junit-ktx:${Version.JunitExtKtx}"
        const val truthExt = "androidx.test.ext:truth:${Version.TruthExt}"
        const val runner = "androidx.test:runner:${Version.TestRunner}"
    }

    object Hilt {
        // dagger hilt
        const val hiltAndroid = "com.google.dagger:hilt-android:${Version.HiltAndroid}"
        const val daggerCompiler = "com.google.dagger:dagger-compiler:${Version.HiltAndroid}"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:${Version.HiltAndroid}"

        //hilt
        const val hiltWork = "androidx.hilt:hilt-work:${Version.HiltNavigationCompose}"
        const val hiltNavigationCompose =
            "androidx.hilt:hilt-navigation-compose:${Version.HiltNavigationCompose}"
    }
}
