//? legacyforge {
package com.springwater.easybot.platforms.legacyforge.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.legacyforge.LegacyForgeEntry;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import com.springwater.easybot.utils.TextUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MessageSyncFeature implements IEasyBotFeatures {
    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        if (ConfigLoader.get().getSkipOptions().isSkipChat()) return;

        if (LegacyForgeEntry.getBridgeClient() == null || !LegacyForgeEntry.getBridgeClient().isReady()) {
            ModData.LOGGER.warn("玩家取消聊天同步,因为服务器未连接主程序");
            return;
        }

        var player = event.getPlayer();
        var message = event.getMessage();

        var playerInfo = PlayerUtils.getPlayerInfo(player);

        EasyBotNetworkingThreadPool.getInstance().addTask(() ->
                LegacyForgeEntry.getBridgeClient().syncMessage(playerInfo, TextUtils.toLegacyString(message), false), "消息同步");
    }
}
//?}
