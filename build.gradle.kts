plugins {
    id("fabric-loom")
    // 不要降级、降级有BUG
    id("com.gradleup.shadow") version "9.3.0"
}

val shade by configurations.creating
configurations.implementation.get().extendsFrom(shade)

version = "${property("mod.version")}+${stonecutter.current.version}"
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
        url = uri("https://maven.nucleoid.xyz/")
        name = "Nucleoid-Text-Placeholder-Api-Repo"
    }
}

dependencies {
    /**
     * 按需导入FabricApi 避免下载用不到的模块
     * @see <a href="https://github.com/FabricMC/fabric">Fabric API模块列表</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, property("deps.fabric_api") as String))
    }
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings(loom.officialMojangMappings()) // emm... 为了更好的Forge开发 或许我们可以尝试一下mojangMappings
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    // TextPlaceholderApi 支持 https://placeholders.pb4.eu/dev/getting-started/
    modImplementation("eu.pb4:placeholder-api:${property("deps.placeholder_api_version")}")

    compileOnly("maven.modrinth:floodgate:${property("deps.floodgate_version")}")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    //
    // 如何将依赖打包到Jar文件中, 用shade!!!!
    //

    shade("com.springwater.easybot:easybot-bridge:${property("deps.bridge_version")}")

    val requiredFApiList = listOf(
        "fabric-entity-events-v1",      // 玩家死亡等数据的处理
        "fabric-networking-api-v1",     // 玩家登录等数据的处理
        "fabric-message-api-v1",        // 专门处理消息同步
        "fabric-lifecycle-events-v1",   // 检测MC服务器启动
    )
    fapi(*requiredFApiList.toTypedArray())
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // 用于接口注入
    accessWidenerPath = rootProject.file("src/main/resources/easybotfabric.accesswidener")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // 为lambda表达式添加名称 - 对于mixins很有用
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // 导出转换后的类以便调试
        runDir = "../../run" // 在版本间共享运行目录
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.shadowJar {
    configurations = listOf(project.configurations.getByName("shade"))
    mergeServiceFiles()
    relocate("javax.websocket", "com.springwater.easybot.libs.javax.websocket")
    relocate("org.eclipse.jetty", "com.springwater.easybot.libs.eclipse.jetty")
    relocate("com.google.gson", "com.springwater.easybot.libs.com.google.gson")
    exclude("META-INF/maven/**")
    exclude("about.html")
}

tasks {
    remapJar {
        dependsOn("shadowJar")
        inputFile.set(shadowJar.flatMap { it.archiveFile })
    }
    build {
        dependsOn(remapJar)
    }
}

tasks {
    processResources {
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // 打包后的文件会在 `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        exclude { it.name.endsWith("-sources.jar") } // 这文件干嘛的,不需要 :D
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}