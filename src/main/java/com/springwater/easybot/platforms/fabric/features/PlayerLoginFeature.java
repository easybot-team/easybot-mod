//? fabric {
package com.springwater.easybot.platforms.fabric.features;

import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.mixin.ServerLoginNetworkHandlerAccessor;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.platforms.common.CommonPlayerLoginFeature;
import com.springwater.easybot.statistic.StatisticManager;
import com.springwater.easybot.utils.FloodgateUtils;
import com.springwater.easybot.utils.GameProfileUtils;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerLoginFeature implements IEasyBotFeatures {
    @Override
    public void register() {
        CommonPlayerLoginFeature.setHandler((handler) -> {
            var profile = ((ServerLoginNetworkHandlerAccessor) handler).getGameProfile();
            var name = GameProfileUtils.getName(profile);
            var uuid = UUID.fromString(GameProfileUtils.getUuid(profile));

            PlayerInfo floodgatePlayer = FloodgateUtils.getFloodgatePlayerInfo(uuid);
            if (floodgatePlayer != null) {
                name = floodgatePlayer.getPlayerName();
                uuid = UUID.fromString(floodgatePlayer.getPlayerUuid());
            }

            // 缓存玩家信息
            StatisticManager.getInstance().getStatDb().putUuidCache(name, uuid);

            if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                handleError(handler, "当前服务器未连接到主程序");
                return;
            }
            var remoteAddress = (InetSocketAddress) ((ServerLoginNetworkHandlerAccessor) handler).GetConnection().getRemoteAddress();
            EasyBotModImpl.INSTANCE.getBridgeClient().reportPlayer(name, uuid.toString(), remoteAddress.getAddress().getHostAddress());
            try {
                var resp = EasyBotModImpl.INSTANCE.getBridgeClient().login(name, uuid.toString());
                if (resp.getKick()) {
                    handler.disconnect(Component.literal(resp.getKickMessage()));
                }
            } catch (Exception e) {
                handleError(handler, e.getLocalizedMessage());
                ModData.LOGGER.error(e.toString());
            }
        });
        
        
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            //? 1.20.1 {
            /*if(ConfigLoader.get().getFabric().isUseMixinReport1201()) return;
            *///?}
            // 这会让客户端停留在 "通讯数据中..."
            synchronizer.waitFor(CompletableFuture.runAsync(() -> CommonPlayerLoginFeature.getHandler().onLoginStart(handler)));
        });
    }

    private void handleError(ServerLoginPacketListenerImpl handler, String reason) {
        // 如果配置忽略错误，仅警告并放行
        if (ConfigLoader.get().isIgnoreError()) {
            ModData.LOGGER.warn(reason + " (已忽略强制验证)");
            return;
        }
        ModData.LOGGER.error(reason);
        handler.disconnect(Component.literal("§c服务器内部异常,请稍后重试!"));
    }
}
//?}