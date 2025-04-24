plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.9.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.10-1.0.31")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.31")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.1.10")
    implementation("org.jetbrains.kotlin.plugin.parcelize:org.jetbrains.kotlin.plugin.parcelize.gradle.plugin:2.1.10")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.56.1")
}