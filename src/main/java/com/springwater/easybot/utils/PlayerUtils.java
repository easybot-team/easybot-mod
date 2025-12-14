package com.springwater.easybot.utils;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.InetSocketAddress;

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
        var remoteAddress = (InetSocketAddress)player.connection.getRemoteAddress();
        playerInfo.setIp(remoteAddress.getHostName());

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
}
