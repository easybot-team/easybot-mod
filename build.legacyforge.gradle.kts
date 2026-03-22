import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.neoforged.moddevgradle.legacyforge.tasks.RemapJar

plugins {
    id("common")
    id("net.neoforged.moddev.legacyforge")
}

version = "forge-${property("mod.version")}+mc${property("mod.mc_dep_display")}"
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
    }

    compileOnly("org.spongepowered:mixin:0.8.5:processor")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.apache.logging.log4j:log4j-api:2.25.3")
}


mixin {
    val refMap = add(sourceSets["main"], "easybot.mixins.refmap.json")
    tasks.shadowJar{
        from(refMap)
    }
    config("easybot.mixins.json")
}

tasks.shadowJar {
    finalizedBy("reobfShadowJar")
    archiveClassifier.set("")
    from(sourceSets["main"].output.classesDirs)
    from(sourceSets["main"].output.resourcesDir)
    addMultiReleaseAttribute.set(false)
    manifest {
        attributes(
            "MixinConfigs" to "easybot.mixins.json",
            "Multi-Release" to "false"
        )
    }
}

legacyForge {
    version = "${property("deps.minecraft")}-${property("deps.forge")}"
    validateAccessTransformers = true

    runs {
        register("server") {
            jvmArgument("-Dmixin.debug.export=true")
            programArgument("--nogui")
            gameDirectory = file("../../run")
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

tasks {
    processResources {
        exclude("**/neoforge.mods.toml", "**/fabric.mod.json")
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        val refmapName = "easybot.mixins.refmap.json"
        inputs.property("mixin_java", mixinJava)
        filesMatching("*.mixins.json") {
            expand("java" to mixinJava)
            // 非常暴力的 refmap 注入办法 没招了 真没招了
            filter { it.replace("\"package\":", "\"refmap\": \"${refmapName}\",\n  \"package\":") }
        }

        filesMatching("mod.package.json") {
            expand(
                "loader" to "legacyforge"
            )
        }
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(named<RemapJar>("reobfShadowJar").map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

obfuscation {
    reobfuscate(tasks.named<ShadowJar>("shadowJar"), sourceSets["main"]) {
    }
}

tasks.named("reobfJar") {
    enabled = false
}

tasks.named("assemble") {
    dependsOn("reobfShadowJar")
}