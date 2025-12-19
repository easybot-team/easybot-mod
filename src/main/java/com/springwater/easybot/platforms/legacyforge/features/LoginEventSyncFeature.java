//? legacyforge {
/*package com.springwater.easybot.platforms.legacyforge.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.legacyforge.LegacyForgeEntry;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LoginEventSyncFeature implements IEasyBotFeatures {
    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ConfigLoader.get().getSkipOptions().isSkipJoin()) return;
        if (LegacyForgeEntry.getBridgeClient() == null || !LegacyForgeEntry.getBridgeClient().isReady()) {
            ModData.LOGGER.warn("玩家取消玩家上线同步,因为服务器未连接主程序");
            return;
        }

        var playerInfo = PlayerUtils.getPlayerInfo(player);
        EasyBotNetworkingThreadPool.getInstance().addTask(() ->
                LegacyForgeEntry.getBridgeClient().syncEnterExit(playerInfo, true), "消息同步-进服");
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (ConfigLoader.get().getSkipOptions().isSkipQuit()) return;

        if (LegacyForgeEntry.getBridgeClient() == null || !LegacyForgeEntry.getBridgeClient().isReady()) {
            ModData.LOGGER.warn("玩家取消玩家下线同步,因为服务器未连接主程序");
            return;
        }

        var playerInfo = PlayerUtils.getPlayerInfo(player);
        EasyBotNetworkingThreadPool.getInstance().addTask(() ->
                LegacyForgeEntry.getBridgeClient().syncEnterExit(playerInfo, false), "消息同步-退服");
    }
}
*///?}
