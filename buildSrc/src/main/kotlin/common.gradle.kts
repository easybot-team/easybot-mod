plugins {
    id("java-library")
    id("com.gradleup.shadow")
}

val shade by configurations.creating
configurations.implementation.get().extendsFrom(shade)

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")

    maven {
        url = uri("https://maven.pkg.github.com/easybot-team/easybot-bridge")
        credentials {
            username = System.getenv("USERNAME")
            password = System.getenv("TOKEN")
        }
    }

    maven {
        url = uri("https://maven.pkg.github.com/easybot-team/ez-statistic")
        credentials {
            username = System.getenv("USERNAME")
            password = System.getenv("TOKEN")
        }
    }
    
    maven("https://www.cursemaven.com") {
        name = "CurseForge"
        content {
            includeGroup("curse.maven")
        }
    }

    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }
}
dependencies {
    
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    //
    // 如何将依赖打包到Jar文件中, 用shade!!!!
    //

    shade("com.springwater.easybot:easybot-bridge:${property("deps.bridge_version")}")
    shade("com.ezylang:EvalEx:3.5.0")
}

tasks.shadowJar {
    configurations = listOf(project.configurations.getByName("shade"))
    mergeServiceFiles()
    relocate("javax.websocket", "com.springwater.easybot.libs.javax.websocket")
    relocate("org.eclipse.jetty", "com.springwater.easybot.libs.eclipse.jetty")
    relocate("com.google.gson", "com.springwater.easybot.libs.com.google.gson")
    relocate("net.minidev", "com.springwater.easybot.libs.net.minidev")
    relocate("com.jayway", "com.springwater.easybot.libs.com.jayway")
    relocate("org.objectweb.asm", "com.springwater.easybot.libs.org.objectweb.asm")
    relocate("com.ezylang.evalex", "com.springwater.easybot.libs.com.ezylang.evalex")
    exclude("META-INF/maven/**")
    exclude("about.html")
    exclude("org/slf4j/**")
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))
        inputs.property("neoforge", project.property("deps.neoforge"))
        inputs.property("minecraft_range", project.property("forge.minecraft.range"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "minecraft_range" to project.property("forge.minecraft.range"),
            "neoforge" to project.property("deps.neoforge")
        )

        filesMatching("fabric.mod.json") { expand(props) }
        filesMatching("**/neoforge.mods.toml") { expand(props) }

        //val mixinJava = "JAVA_${requiredJava.majorVersion}"
        //filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }
}