pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://neoforged.forgecdn.net/releases") {  name = "NeoForge" }
        maven("https://maven.minecraftforge.net") { name = "Forge" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        all {
            if (this is MavenArtifactRepository) {
                if (url.toString().contains("maven.neoforged.net")) {
                    content {
                        excludeGroup("org.apache.logging.log4j")
                    }
                }
            }
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.1-beta.4"
}

stonecutter {
    create(rootProject) {
        mapBuilds { _, data ->
            val loader = data.project.substringAfterLast('-')
            if (loader == "fabric") {
                if (stonecutter.eval(data.version, ">=26.1")) {
                    "build.fabric-new.gradle.kts" // 新版本配置
                } else {
                    "build.fabric-old.gradle.kts"
                }
            } else {
                "build.$loader.gradle.kts"
            }
        }
        fun match(v: String, vararg loaders: String) {
            loaders.forEach { version("$v-$it", v) }
        }

        match("1.20.1", "fabric", "legacyforge")
        match("1.20.2", "fabric")
        match("1.20.4", "fabric", "neoforge")
        match("1.20.6", "fabric", "neoforge")
        match("1.21", "fabric", "neoforge")
        match("1.21.6", "fabric", "neoforge")
        match("1.21.8", "fabric", "neoforge")
        match("1.21.9", "fabric", "neoforge")
        match("1.21.10", "fabric", "neoforge")
        match("1.21.11", "fabric", "neoforge")
        match("26.1", "fabric", "neoforge")
        match("26.1.1", "fabric", "neoforge")
        match("26.1.2", "fabric", "neoforge")

        vcsVersion = "1.21.11-fabric"
    }
}

rootProject.name = "EasyBotMod"