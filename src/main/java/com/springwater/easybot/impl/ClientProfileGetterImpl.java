package com.springwater.easybot.impl;

import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;
//? fabric {
/*import net.fabricmc.loader.api.FabricLoader;
 *///?}
import net.minecraft.server.MinecraftServer;
//? neoforge {
/*import net.neoforged.fml.loading.FMLLoader;
*///?}

public class ClientProfileGetterImpl {
    public void BuildClientProfile(MinecraftServer server) {
        //? fabric {
        /*FabricLoader fabricLoader = FabricLoader.getInstance();
        ClientProfile.setServerDescription(fabricLoader.getRawGameVersion());
        *///?}
        //? neoforge {
        /*//? > 1.21.8 {
        /^ClientProfile.setServerDescription(FMLLoader.getCurrent().getVersionInfo().mcVersion());
         ^///?}
        //? <= 1.21.8 {
        ClientProfile.setServerDescription(FMLLoader.versionInfo().mcVersion());
        //?}
        *///?}
        //? legacyforge {        
        ClientProfile.setServerDescription(server.getServerVersion());
        // }
        ClientProfile.setPluginVersion(ModData.VERSION);
        ClientProfile.setDebugMode(ConfigLoader.get().isDebug());
        ClientProfile.setOnlineMode(server.usesAuthentication()); // usesAuthentication(yarn: IsOnlineMode)
        ClientProfile.setCommandSupported(true);
        ClientProfile.setPapiSupported(true);
    }
}
