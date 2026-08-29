package com.springwater.easybot.clientip;

import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class ClientIpBridgeForwarder {
    private ClientIpBridgeForwarder() {
    }

    public static void forward(ServerPlayer player, String ip) {
        if (player == null || ip == null) {
            return;
        }

        String normalized = ip.trim();
        if (normalized.isEmpty()) {
            return;
        }

        ClientIpCache.put(player.getUUID(), normalized);
        if (EasyBotModImpl.INSTANCE.getBridgeClient() == null || !EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
            ModData.LOGGER.warn("客户端IP上报已接收,但桥接客户端未就绪,已仅缓存 {} -> {}", player.getName().getString(), normalized);
            return;
        }

        var playerInfo = PlayerUtils.getPlayerInfo(player);
        playerInfo.setIp(normalized);
        String playerName = playerInfo.getName();
        String playerUuid = playerInfo.getUuid();

        EasyBotNetworkingThreadPool.getInstance().addTask(() -> {
            var bridgeClient = EasyBotModImpl.INSTANCE.getBridgeClient();
            if (bridgeClient == null || !bridgeClient.isReady()) {
                ModData.LOGGER.warn("客户端IP上报任务执行时桥接客户端已离线,已丢弃 {} -> {}", playerName, normalized);
                return;
            }
            bridgeClient.reportPlayer(playerName, playerUuid, normalized);
        }, "客户端IP上报");
    }

    public static void forward(String playerName, String playerUuid, String ip) {
        if (playerUuid == null || playerName == null) {
            return;
        }

        ServerPlayer player = null;
        try {
            player = EasyBotModImpl.INSTANCE.getServer().getPlayerList().getPlayer(UUID.fromString(playerUuid));
        } catch (Exception ignored) {
        }

        if (player == null) {
            player = EasyBotModImpl.INSTANCE.getServer().getPlayerList().getPlayerByName(playerName);
        }

        if (player == null) {
            ModData.LOGGER.warn("客户端IP上报未能定位到在线玩家 {} ({})", playerName, playerUuid);
            return;
        }

        forward(player, ip);
    }
}
