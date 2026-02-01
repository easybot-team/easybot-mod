//? legacyforge {
package com.springwater.easybot.platforms.legacyforge.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.legacyforge.LegacyForgeEntry;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.CarpetUtils;
import com.springwater.easybot.utils.PlayerUtils;
import com.springwater.easybot.utils.TextUtils;
import net.minecraftforge.event.ServerChatEvent;

public class MessageSyncFeature implements IEasyBotFeatures {
    @Override
    public void register() {
        // 这里不使用Forge的事件总线订阅ServerChatEvent
        // 这是因为我发现无法在Mohist上触发,已在此处使用Mixin实现
    }
    
    public static void onChatMessage(ServerChatEvent event) {
        if (ConfigLoader.get().getSkipOptions().isSkipChat()) return;
        if (LegacyForgeEntry.getBridgeClient() == null || !LegacyForgeEntry.getBridgeClient().isReady()) {
            ModData.LOGGER.warn("玩家取消聊天同步,因为服务器未连接主程序");
            return;
        }

        var player = event.getPlayer();
        if (CarpetUtils.isFakePlayer(player)) {
            if (ConfigLoader.get().isDebug()) {
                ModData.LOGGER.info("已过滤地毯假人 {}", player.getName().getString());
            }
            return;
        }
        var message = event.getRawText();
        var playerInfo = PlayerUtils.getPlayerInfo(player);

        EasyBotNetworkingThreadPool.getInstance().addTask(() ->
                LegacyForgeEntry.getBridgeClient().syncMessage(playerInfo, message, false), "消息同步");
    }
}
//?}
