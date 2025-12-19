//? fabric {
package com.springwater.easybot.platforms.fabric.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.utils.CarpetUtils;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class MessageSyncFeature implements IEasyBotFeatures {
    @Override
    public void register() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (ConfigLoader.get().getSkipOptions().isSkipChat()) return;
            if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                ModData.LOGGER.warn("玩家取消聊天同步,因为服务器未连接主程序");
                return;
            }
            if (CarpetUtils.isFakePlayer(sender)) {
                if (ConfigLoader.get().isDebug()) {
                    ModData.LOGGER.info("已过滤地毯假人 {}", sender.getName().getString());
                }
                return;
            }
            var playerInfo = PlayerUtils.getPlayerInfo(sender); // 注意,最好不要在别的线程获取,你永远不知道下一个tick数据是否可用
            EasyBotNetworkingThreadPool.getInstance().addTask(() -> EasyBotModImpl.INSTANCE.getBridgeClient().syncMessage(playerInfo, message.signedContent(), false), "消息同步");
        });
    }
}
//?}