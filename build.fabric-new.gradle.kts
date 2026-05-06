import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.shadowJar

plugins {
    id("common")
    id("net.fabricmc.fabric-loom")
}

version = "fabric-${property("mod.version")}+mc${property("mod.mc_dep_display")}"
base.archivesName = property("mod.id") as String
val requiredJava = when {
    stonecutter.eval(stonecutter.current.version, ">=26.1") -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_1_8
}

repositories {
    maven {
        url = uri("https://maven.nucleoid.xyz/")
        name = "Nucleoid-Text-Placeholder-Api-Repo"
    }
}

dependencies {
    fun fapi(vararg modules: String) {
        for (it in modules) add("compileOnly",fabricApi.module(it, property("deps.fabric_api") as String))
    }

    add("minecraft", "com.mojang:minecraft:${property("deps.minecraft")}")

    add("compileOnly", "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    add("compileOnly", "eu.pb4:placeholder-api:${property("deps.placeholder_api_version")}")

    add("compileOnly", "maven.modrinth:floodgate:${property("deps.floodgate_version")}")
    add("compileOnly", "maven.modrinth:easyauth:${property("deps.easyauth_version")}")

    val requiredFApiList = listOf(
        "fabric-entity-events-v1",
        "fabric-networking-api-v1",
        "fabric-message-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-command-api-v2",
    )
    fapi(*requiredFApiList.toTypedArray())

    add("shade", "com.springwater.easybot:ez-statistic:${property("deps.ez_statistic_version")}")
}

configure<LoomGradleExtensionAPI> {
    //accessWidenerPath.set(rootProject.file("src/main/resources/easybot.accesswidener"))

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25) // Java 25
}

tasks {
    val shadowJarProvider = named<Jar>("shadowJar")
    named("build") {
        dependsOn(shadowJarProvider)
    }

    shadowJar {
        archiveClassifier = ""
    }

    named<ProcessResources>("processResources") {
        exclude("**/neoforge.mods.toml", "**/mods.toml")
        val mixinJava = "JAVA_25"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        filesMatching("mod.package.json") {
            expand("loader" to "fabric")
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"

        from(shadowJarProvider.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}