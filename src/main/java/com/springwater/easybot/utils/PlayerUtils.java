package com.springwater.easybot.utils;
import com.springwater.easybot.clientip.ClientIpCache;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

public class PlayerUtils {
    /**
     * 踢出玩家 (线程安全)
     *
     * @param name   玩家名
     * @param reason 理由
     */
    public static void kickPlayerAsync(String name, String reason) {
        EasyBotModImpl.INSTANCE.getServer().execute(() -> {
            ServerPlayer player = EasyBotModImpl.INSTANCE.getServer().getPlayerList().getPlayerByName(name);
            if (player == null) {
                ModData.LOGGER.warn("踢出玩家失败: 玩家{}不存在", name);
                return;
            }
            kickPlayerSync(player, reason);
        });
    }

    /**
     * 踢出玩家 (注意!!!!!! 调用此方法必须确保你当前在服务器主线程!!!!!)
     *
     * @see #kickPlayerSync(ServerPlayer, String) 其他线程必须使用此方法
     */
    public static void kickPlayerSync(ServerPlayer player, String reason) {
        player.connection.disconnect(Component.literal(reason));
    }

    public static PlayerInfoWithRaw getPlayerInfo(ServerPlayer player) {
        PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
        playerInfo.setName(player.getName().getString());
        playerInfo.setNameRaw(player.getName().getString());
        playerInfo.setUuid(player.getUUID().toString());
        playerInfo.setIp(getPlayerIp(player));

        if (FloodgateUtils.isFloodgatePlayer(player.getUUID())) {
            var floodgateInfo = FloodgateUtils.getFloodgatePlayerInfo(player.getUUID());
            if (floodgateInfo != null) {
                playerInfo.setName(floodgateInfo.getPlayerName());
                playerInfo.setNameRaw(floodgateInfo.getPlayerName());
                playerInfo.setUuid(floodgateInfo.getPlayerUuid());
            } else {
                ModData.LOGGER.warn("玩家{}的Floodgate信息获取失败", player.getName().getString());
            }
        }

        return playerInfo;
    }

    public static String getPlayerIp(ServerPlayer player) {
        String clientIp = ClientIpCache.get(player.getUUID());
        if (clientIp != null && !clientIp.isBlank()) {
            if (ConfigLoader.get().isDebug()) {
                ModData.LOGGER.info("玩家{}使用客户端上报的公网IP {}", player.getName().getString(), clientIp);
            }
            return clientIp;
        }

        var ip = getRemoteIp(getRemoteAddress(player));
        if (Objects.equals(ip, "127.0.0.1") && ConfigLoader.get().isDebug()) {
            ModData.LOGGER.info("玩家{}没有真实网络地址, 已使用回环IP", player.getName().getString());
        }
        return ip;
    }

    public static boolean hasRemoteAddress(ServerPlayer player) {
        return getRemoteAddress(player) != null;
    }

    public static String getRemoteIp(SocketAddress remoteAddress) {
        if (remoteAddress instanceof InetSocketAddress inetSocketAddress && inetSocketAddress.getAddress() != null) {
            return inetSocketAddress.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    private static SocketAddress getRemoteAddress(ServerPlayer player) {
        if (player.connection == null) return null;
        return player.connection.getRemoteAddress();
    }
}
