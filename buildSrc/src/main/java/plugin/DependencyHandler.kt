import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandler.annotationProcessor(dependencyNotation: Any): Dependency? =
    add("annotationProcessor", dependencyNotation)

fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
    add("testImplementation", dependencyNotation)

fun DependencyHandler.androidTestImplementation(dependencyNotation: Any): Dependency? =
    add("androidTestImplementation", dependencyNotation)

fun DependencyHandler.ksp(dependencyNotation: Any): Dependency? =
    add("ksp", dependencyNotation)

fun DependencyHandler.composeDependencies() {
    implementation(platform(Libraries.Compose.composeBom))
    implementation(Libraries.Compose.composeActivity)
    implementation(Libraries.Compose.composeUi)
    implementation(Libraries.Compose.composeUiToolingPreview)
    implementation(Libraries.Compose.composeUiUtil)
    implementation(Libraries.Compose.composeFoundation)
    implementation(Libraries.Compose.composeRuntime)
    implementation(Libraries.Compose.composeMaterial3)
    implementation(Libraries.Compose.composeMaterial)
    //navigation
    implementation(Libraries.Navigation.navigationCompose)

    //coil
    implementation(Libraries.Coil.coilCompose)

    //hilt navigation
    implementation(Libraries.Hilt.hiltNavigationCompose)

    //accompanist
    accompanistDependencies()

    //constraint layout
    implementation(Libraries.Compose.constraintLayoutCompose)

    //timber
    implementation(Libraries.Timber.timber)

    //Firebase
    implementation(platform(Libraries.Google.Firebase.bom))
    implementation(Libraries.Google.Firebase.crashlytics)

    // Maps, Places, PlayServices
    implementation(Libraries.Google.PlayServices.map)
    implementation(Libraries.Google.PlayServices.location)
    implementation(Libraries.Google.maps)
    implementation(Libraries.Google.places)

    //room
    implementation(Libraries.Room.runtime)
    implementation(Libraries.Room.ktx)
    annotationProcessor(Libraries.Room.compiler)
    ksp(Libraries.Room.compiler)

    // Security
    implementation(Libraries.AndroidX.security)
}

fun DependencyHandler.accompanistDependencies() {
    implementation(Libraries.Accompanist.pager)
    implementation(Libraries.Accompanist.swiperefresh)
    implementation(Libraries.Accompanist.indicators)
    implementation(Libraries.Accompanist.systemuicontroller)
    implementation(Libraries.Accompanist.navigationMaterial)
    implementation(Libraries.Accompanist.navigationAnimation)
    implementation(Libraries.Accompanist.permission)
    implementation(Libraries.Accompanist.flowLayout)
}

fun DependencyHandler.baseDependencies() {
    implementation(Libraries.AndroidX.appCompat)
    implementation(Libraries.AndroidX.coreKtx)
    implementation(Libraries.AndroidX.multiDex)

    //Lifecycle
    implementation(Libraries.AndroidX.lifecycleRunTimeKtx)
    implementation(Libraries.AndroidX.lifecycleRunTimeCompose)

    //ViewModel
    implementation(Libraries.AndroidX.viewModelKtx)
    implementation(Libraries.AndroidX.viewModelCompose)

    //LiveData
    implementation(Libraries.AndroidX.liveData)

    //WorkManager
    implementation(Libraries.AndroidX.workManager)

    //Lottie
    implementation(Libraries.Lottie.lottie)

    implementation(Libraries.Google.gson)
    implementation(Libraries.Hilt.hiltAndroid)
    implementation(Libraries.Hilt.hiltWork)
    ksp(Libraries.Hilt.hiltAndroidCompiler)

    implementation(Libraries.Google.guava)

    //okhttp
    implementation(Libraries.SquareUp.okhttp3)
    implementation(Libraries.SquareUp.okhttp3LoggingInterceptor)

    //retrofit
    implementation(Libraries.SquareUp.retrofit2)
    implementation(Libraries.SquareUp.converterGson)

    //moshi
    implementation(Libraries.SquareUp.moshi)
    implementation(Libraries.SquareUp.converterMoshi)
    ksp(Libraries.SquareUp.moshiCodegen)

    //apollo
    implementation(Libraries.Apollo.apollo3)

    //supabase
//    implementation(Libraries.Supabase.auth)

    //coroutines
    implementation(Libraries.Coroutine.android)
    implementation(Libraries.Coroutine.core)

    //kotlinreflect
    implementation(Libraries.KotlinReflect.reflect)
}

fun DependencyHandler.testDependencies() {
//    runtimeOnly(Libraries.Test.testCoreKtx)
    androidTestImplementation(Libraries.Test.espressorCore)
    androidTestImplementation(Libraries.Test.runner)
    androidTestImplementation(Libraries.Test.junitExtKtx)
    androidTestImplementation(Libraries.Test.truthExt)

    //Coroutine
    implementation(Libraries.Coroutine.test)
}

fun DependencyHandler.moduleDependencies() {
    CORE
    DATA
    DOMAIN
    COMMON_THEME
    COMMON_COMPOSABLE
    FEATURE_HOME
}

val DependencyHandler.CORE
    get() = implementation(project(mapOf("path" to ":core")))

val DependencyHandler.COMMON_THEME
    get() = implementation(project(mapOf("path" to ":common:theme")))

val DependencyHandler.COMMON_COMPOSABLE
    get() = implementation(project(mapOf("path" to ":common:composable")))

val DependencyHandler.DATA
    get() = implementation(project(mapOf("path" to ":data")))

val DependencyHandler.DOMAIN
    get() = implementation(project(mapOf("path" to ":domain")))

val DependencyHandler.FEATURE_HOME
    get() = implementation(project(mapOf("path" to ":feature:home")))