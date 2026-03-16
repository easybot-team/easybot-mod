//? neoforge {
/*package com.springwater.easybot.platforms.neoforge;
import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NeoForgeModImpl implements EasyBotModImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModData.MOD_ID);

    @Override
    public String getLoaderName() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public MinecraftServer getServer() {
        return NeoForgeEntry.getServer();
    }

    @Override
    public BridgeClient getBridgeClient() {
        return NeoForgeEntry.getBridgeClient();
    }

    @Override
    public boolean isAuthenticated(String playerName) {
        return true; // 未实现
    }
}
*///?}