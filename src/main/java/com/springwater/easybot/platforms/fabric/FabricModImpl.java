//? fabric {
package com.springwater.easybot.platforms.fabric;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nikitacartes.easyauth.interfaces.PlayerAuth;

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

    @Override
    public boolean isAuthenticated(String playerName) {
        try {
            if (!isModLoaded("easyauth"))
                return true; // 未实现
            ServerPlayer player = FabricEntry.getServer().getPlayerList().getPlayerByName(playerName);
            if (player == null) return true;
            PlayerAuth playerAuth = (PlayerAuth) player;
            return playerAuth.easyAuth$isAuthenticated();
        } catch (Exception e) {
            LOGGER.error("无法获取到玩家状态,请确保您的EasyAuth版本>=3.4.2 : {}", e.toString());
            return true;
        }
    }
}
//?}