plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.kikugie.dev/snapshots")
}

dependencies {
    implementation("dev.kikugie:stonecutter:0.7.6")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.3.0")
}