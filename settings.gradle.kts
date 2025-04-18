pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://jitpack.io") // Cho Supabase
//        val flutterStorageUrl = System.getenv("FLUTTER_STORAGE_BASE_URL") ?: "https://storage.googleapis.com"
//        maven(url = "C:\\Users\\Admin\\qualgoo\\channel_flutter\\build\\host\\outputs\\repo")
//        maven(url = "$flutterStorageUrl/download.flutter.io")
    }
}

rootProject.name = "Kyobi Customer"
include(":app")
include(":core")
include(":common:theme")
include(":common:composable")
include(":data")
include(":domain")
include(":feature:home")
include(":feature:authentication")
include(":feature:profile")
