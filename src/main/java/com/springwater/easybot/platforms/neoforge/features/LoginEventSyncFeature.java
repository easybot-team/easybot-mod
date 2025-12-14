//? neoforge {
package com.springwater.easybot.platforms.neoforge.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class LoginEventSyncFeature implements IEasyBotFeatures {

    @Override
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        var player = (ServerPlayer) event.getEntity();
        if (ConfigLoader.get().getSkipOptions().isSkipJoin()) return;
        if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
            ModData.LOGGER.warn("玩家取消玩家上线同步,因为服务器未连接主程序");
            return;
        }
        // 注意,最好不要在别的线程获取,你永远不知道下一个tick数据是否可用
        var playerInfo = PlayerUtils.getPlayerInfo(player);
        EasyBotNetworkingThreadPool.getInstance().addTask(() -> EasyBotModImpl.INSTANCE.getBridgeClient().syncEnterExit(playerInfo, true), "消息同步-进服");
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        var player =(ServerPlayer) event.getEntity();
        if (ConfigLoader.get().getSkipOptions().isSkipQuit()) return;
        if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
            ModData.LOGGER.warn("玩家取消玩家下线同步,因为服务器未连接主程序");
            return;
        }
        // 注意,最好不要在别的线程获取,你永远不知道下一个tick数据是否可用
        var playerInfo = PlayerUtils.getPlayerInfo(player);
        EasyBotNetworkingThreadPool.getInstance().addTask(() -> EasyBotModImpl.INSTANCE.getBridgeClient().syncEnterExit(playerInfo, false), "消息同步-退服");
    }
}

//?}