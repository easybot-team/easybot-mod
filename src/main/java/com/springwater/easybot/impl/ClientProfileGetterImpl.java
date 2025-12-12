package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.config.ConfigLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class ClientProfileGetterImpl {
    public void BuildClientProfile(MinecraftServer server) {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        ClientProfile.setPluginVersion(EasyBotFabric.VERSION);
        ClientProfile.setServerDescription(fabricLoader.getRawGameVersion());
        ClientProfile.setDebugMode(ConfigLoader.get().isDebug());
        ClientProfile.setOnlineMode(server.usesAuthentication()); // usesAuthentication(yarn: IsOnlineMode)
        ClientProfile.setCommandSupported(true);
        ClientProfile.setPapiSupported(true);
    }
}
