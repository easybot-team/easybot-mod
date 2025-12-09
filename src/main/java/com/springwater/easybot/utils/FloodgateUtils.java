package com.springwater.easybot.utils;

import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.config.ConfigLoader;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FloodgateUtils {
    @Nullable
    public static PlayerInfo getFloodgatePlayerInfo(UUID uuid) {
        if (!ClientProfile.isHasFloodgate()) {
            return null;
        }
        var floodgatePlayer = FloodgateApi.getInstance().getPlayer(uuid);
        if (floodgatePlayer == null) return null;
        var playerInfo = new PlayerInfo();
        playerInfo.setPlayerUuid(
                ConfigLoader.get().getGeyser().isUseRealUuid() ?
                        floodgatePlayer.getCorrectUniqueId().toString() :
                        floodgatePlayer.getJavaUniqueId().toString()
        );

        playerInfo.setPlayerName(
                ConfigLoader.get().getGeyser().isIgnorePrefix() ?
                        floodgatePlayer.getCorrectUsername() :
                        floodgatePlayer.getJavaUsername()
        );
        return playerInfo;
    }
    
    public static boolean isFloodgatePlayer(UUID uuid) {
        if (!ClientProfile.isHasFloodgate()) {
            return false;
        }
        return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }
}
