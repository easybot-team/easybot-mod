package com.springwater.easybot.utils;

import com.springwater.easybot.bridge.model.PlayerInfo;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class PlayerInfoUtils {
    public static List<PlayerInfo> buildPlayerInfoList(List<ServerPlayer> players) {
        return players.stream().filter(player -> !CarpetUtils.isFakePlayer(player)).map(player -> {
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.setPlayerName(player.getName().getString());
            playerInfo.setPlayerUuid(player.getUUID().toString());
            playerInfo.setIp(player.connection.getRemoteAddress().toString());
            playerInfo.setSkinUrl(SkinUtils.getSkinUrl(player));
            playerInfo.setBedrock(
                    FloodgateUtils.isFloodgatePlayer(player.getUUID())
            );
            return playerInfo;
        }).toList();
    }
}
