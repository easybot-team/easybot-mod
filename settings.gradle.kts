pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://neoforged.forgecdn.net/releases") { name = "NeoForge" }
        maven("https://maven.minecraftforge.net") { name = "Forge" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    
    create(rootProject) {
        fun match(version: String, vararg loaders: String) = loaders
            .forEach { vers("$version-$it", version).buildscript = "build.$it.gradle.kts" }

        match("1.20.1", "fabric", "forge") //  NeoForge不支持1.20.1
        match("1.20.2", "fabric", "forge")
        match("1.20.4", "fabric", "forge", "neoforge")
        match("1.21.1", "fabric", "forge", "neoforge")
        match("1.21.8", "fabric", "forge", "neoforge")
        match("1.21.9", "fabric", "forge", "neoforge")
        match("1.21.10", "fabric", "forge", "neoforge")
        vcsVersion = "1.21.10-fabric"
    }
}

rootProject.name = "EasyBotMod"