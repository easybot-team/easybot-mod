//? fabric {
/*package com.springwater.easybot.platforms.fabric;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class FabricModImpl implements EasyBotModImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModData.MOD_ID);

    @Override
    public String getLoaderName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public MinecraftServer getServer() {
        return FabricEntry.getServer();
    }

    @Override
    public BridgeClient getBridgeClient() {
        return FabricEntry.getBridgeClient();
    }
}
*///?}