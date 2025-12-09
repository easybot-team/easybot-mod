package com.springwater.easybot.features;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.mixin.ServerLoginNetworkHandlerAccessor;
import com.springwater.easybot.utils.GameProfileUtils;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class PlayerLoginFeature implements IEasyBotFeatures {
    @Override
    public void register() {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            // 这会让客户端停留在 "通讯数据中..."
            synchronizer.waitFor(CompletableFuture.runAsync(() -> {
                if (!EasyBotFabric.getBridgeClient().isReady()) {
                    handleError(handler, "当前服务器未连接到主程序");
                    return;
                }
                var profile = ((ServerLoginNetworkHandlerAccessor) handler).getGameProfile();
                var name = GameProfileUtils.getName(profile);
                var uuid = GameProfileUtils.getUuid(profile);
                var remoteAddress = (InetSocketAddress)((ServerLoginNetworkHandlerAccessor) handler).GetConnection().getRemoteAddress();
                EasyBotFabric.getBridgeClient().reportPlayer(name, uuid, remoteAddress.getHostName());
                try {
                    var resp = EasyBotFabric.getBridgeClient().login(name, uuid);
                    if (resp.getKick()) {
                        handler.disconnect(Component.literal(resp.getKickMessage()));
                    }
                } catch (Exception e) {
                    handleError(handler, e.getLocalizedMessage());
                    EasyBotFabric.LOGGER.error(e.toString());
                }
            }));
        });
    }

    private void handleError(ServerLoginPacketListenerImpl handler, String reason) {
        // 如果配置忽略错误，仅警告并放行
        if (ConfigLoader.get().isIgnoreError()) {
            EasyBotFabric.LOGGER.warn(reason + " (已忽略强制验证)");
            return;
        }
        EasyBotFabric.LOGGER.error(reason);
        handler.disconnect(Component.literal("§c服务器内部异常,请稍后重试!"));
    }
}
