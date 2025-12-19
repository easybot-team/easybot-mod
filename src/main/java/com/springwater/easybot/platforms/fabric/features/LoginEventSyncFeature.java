//? fabric {
package com.springwater.easybot.platforms.fabric.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.utils.CarpetUtils;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class LoginEventSyncFeature implements IEasyBotFeatures {
    @Override
    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            if (CarpetUtils.isFakePlayer(player)) {
                if (ConfigLoader.get().isDebug()) {
                    ModData.LOGGER.info("已过滤地毯假人 {}", player.getName().getString());
                }
                return;
            }

            if (ConfigLoader.get().getSkipOptions().isSkipJoin()) return;
            if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                ModData.LOGGER.warn("玩家取消玩家上线同步,因为服务器未连接主程序");
                return;
            }
            var playerInfo = PlayerUtils.getPlayerInfo(player); // 注意,最好不要在别的线程获取,你永远不知道下一个tick数据是否可用
            EasyBotNetworkingThreadPool.getInstance().addTask(() -> EasyBotModImpl.INSTANCE.getBridgeClient().syncEnterExit(playerInfo, true), "消息同步-进服");
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            if (CarpetUtils.isFakePlayer(player)) {
                if (ConfigLoader.get().isDebug()) {
                    //noinspection LoggingSimilarMessage
                    ModData.LOGGER.info("已过滤地毯假人 {}", player.getName().getString());
                }
                return;
            }
            if (ConfigLoader.get().getSkipOptions().isSkipQuit()) return;
            if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                ModData.LOGGER.warn("玩家取消玩家下线同步,因为服务器未连接主程序");
                return;
            }
            var playerInfo = PlayerUtils.getPlayerInfo(player); // 注意,最好不要在别的线程获取,你永远不知道下一个tick数据是否可用
            EasyBotNetworkingThreadPool.getInstance().addTask(() -> EasyBotModImpl.INSTANCE.getBridgeClient().syncEnterExit(playerInfo, false), "消息同步-退服");
        });
    }
}
//?}