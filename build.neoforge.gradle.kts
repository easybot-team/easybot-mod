plugins {
    id("common")
    id("net.neoforged.moddev")
}

version = "neoforge-${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava = when {
    stonecutter.eval(stonecutter.current.version, ">=1.20.6") -> JavaVersion.VERSION_21
    stonecutter.eval(stonecutter.current.version, ">=1.18") -> JavaVersion.VERSION_17
    // shadowJar旧版本relocate有bug、文档上说新版本理论上已不再兼容Java17以下的版本
    // 当经过测试使用VERSION_1_8也能正常打包,不知道会不会出啥问题,不管了 反正也不打算支持1.17以下的版本(
    // https://github.com/GradleUp/shadow
    stonecutter.eval(stonecutter.current.version, ">=1.17") -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

dependencies {
    compileOnly("maven.modrinth:floodgate:${property("deps.floodgate_version")}")
    shade("com.springwater.easybot:ez-statistic:${property("deps.ez_statistic_version")}") {
        exclude("org.slf4j", "slf4j-api")
        exclude("net.minidev", "json-smart")
        exclude("net.minidev", "json-path")
    }
}

neoForge {
    version = property("deps.neoforge") as String
    validateAccessTransformers = true

    runs {
        register("server") {
            jvmArgument("-Dmixin.debug.export=true")
            gameDirectory = file("../../run")
            programArgument("--nogui")
            server()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
    sourceSets["main"].resources.srcDir("src/main/generated")
}

tasks.shadowJar {
    archiveClassifier = ""
}

tasks.build {
    dependsOn("shadowJar")
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/mods.toml")
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}