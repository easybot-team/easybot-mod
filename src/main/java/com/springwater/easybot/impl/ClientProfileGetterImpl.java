package com.springwater.easybot.impl;

import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.server.MinecraftServer;


public class ClientProfileGetterImpl {
    public void BuildClientProfile(MinecraftServer server) {
        ClientProfile.setServerDescription(server.getServerVersion());
        ClientProfile.setPluginVersion(ModData.VERSION);
        ClientProfile.setDebugMode(ConfigLoader.get().isDebug());
        ClientProfile.setOnlineMode(server.usesAuthentication()); // usesAuthentication(yarn: IsOnlineMode)
        ClientProfile.setCommandSupported(true);
        ClientProfile.setPapiSupported(true);
    }
}
