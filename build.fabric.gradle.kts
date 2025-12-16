plugins {
    id("common")
    id("fabric-loom")
}

version = "fabric-${property("mod.version")}+mc${property("mod.mc_dep_display")}"
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
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    mappings(loom.officialMojangMappings()) // emm... 为了更好的Forge开发 或许我们可以尝试一下mojangMappings
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    // TextPlaceholderApi 支持 https://placeholders.pb4.eu/dev/getting-started/
    modImplementation("eu.pb4:placeholder-api:${property("deps.placeholder_api_version")}")

    compileOnly("maven.modrinth:floodgate:${property("deps.floodgate_version")}")

    val requiredFApiList = listOf(
        "fabric-entity-events-v1",      // 玩家死亡等数据的处理
        "fabric-networking-api-v1",     // 玩家登录等数据的处理
        "fabric-message-api-v1",        // 专门处理消息同步
        "fabric-lifecycle-events-v1",   // 检测MC服务器启动
        "fabric-command-api-v2",        // 注册命令
    )
    fapi(*requiredFApiList.toTypedArray())

    shade("com.springwater.easybot:ez-statistic:${property("deps.ez_statistic_version")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // 用于接口注入
    accessWidenerPath = rootProject.file("src/main/resources/easybot.accesswidener")

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
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
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
        exclude("**/neoforge.mods.toml", "**/mods.toml")
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        filesMatching("mod.package.json") {
            expand(
                "loader" to "fabric"
            )
        }
    }

    // 打包后的文件会在 `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}