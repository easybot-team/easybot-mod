//? legacyforge {
package com.springwater.easybot.platforms.legacyforge;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class LegacyForgeModImpl implements EasyBotModImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModData.MOD_ID);
    @Override
    public String getLoaderName() {
        return "legacyforge";
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
        return LegacyForgeEntry.getServer();
    }

    @Override
    public BridgeClient getBridgeClient() {
        return LegacyForgeEntry.getBridgeClient();
    }
}
//?}