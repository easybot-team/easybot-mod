plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.13-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.123" apply false    
    id("net.minecraftforge.gradle") version "6.0.46" apply false
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0" apply false
}

stonecutter active "1.21.6-fabric"


// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge", "legacyforge")
    swaps["mod_id"] = "\"" + property("mod.id") + "\";"
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    swaps["papi"] = "\"" + node.project.property("deps.placeholder_api_version") + "\";"
    swaps["loader"] = "\"" + node.metadata.project.substringAfterLast('-')[1] + "\";"
    // 如果 存在fapi,则添加依赖
    if (node.project.properties.containsKey("deps.fabric_api")) {
        dependencies["fapi"] = node.project.property("deps.fabric_api") as String
    }
}
