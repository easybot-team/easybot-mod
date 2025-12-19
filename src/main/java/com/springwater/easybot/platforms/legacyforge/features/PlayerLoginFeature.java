//? legacyforge {
/*package com.springwater.easybot.platforms.legacyforge.features;

import com.mojang.authlib.GameProfile;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.legacyforge.LegacyForgeEntry;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.statistic.StatisticManager;
import com.springwater.easybot.utils.FloodgateUtils;
import com.springwater.easybot.utils.GameProfileUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

//? if >=1.19 {
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
//?} else {
/^import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 ^///?}

public class PlayerLoginFeature implements IEasyBotFeatures {

    @Override
    public void register() {
        // 没找到在Forge合适的事件,已使用Mixin实现
    }

    // 返回 true 表示允许进入，false 表示拒绝
    public static Component checkLoginSync(GameProfile profile, SocketAddress address) {
        var name = GameProfileUtils.getName(profile);
        var uuid = UUID.fromString(GameProfileUtils.getUuid(profile));
        PlayerInfo floodgatePlayer = FloodgateUtils.getFloodgatePlayerInfo(uuid);
        if (floodgatePlayer != null) {
            name = floodgatePlayer.getPlayerName();
            uuid = UUID.fromString(floodgatePlayer.getPlayerUuid());
        }
        // 存入缓存
        StatisticManager.getInstance().getStatDb().putUuidCache(name, uuid);

        if (LegacyForgeEntry.getBridgeClient() == null || !LegacyForgeEntry.getBridgeClient().isReady()) {
            return handleError("当前服务器未连接到主程序");
        }
        var remoteAddress = (InetSocketAddress) address;
        String hostName = (remoteAddress != null) ? remoteAddress.getAddress().getHostAddress() : "unknown";
        LegacyForgeEntry.getBridgeClient().reportPlayer(name, uuid.toString(), hostName);
        try {
            var resp = LegacyForgeEntry.getBridgeClient().login(name, uuid.toString());
            if (resp.getKick()) {
                return Component.literal(resp.getKickMessage());
            }
            return null;
        } catch (Exception e) {
            ModData.LOGGER.error(e.toString());
            return handleError(e.getLocalizedMessage());
        }
    }

    private static Component handleError(String reason) {
        if (ConfigLoader.get().isIgnoreError()) {
            ModData.LOGGER.warn("{} (已忽略强制验证)", reason);
            return null;
        }
        ModData.LOGGER.error(reason);
        return Component.literal("§c服务器内部异常,请稍后重试!");
    }
}
*///?}
