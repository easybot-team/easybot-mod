//? legacyforge {
package com.springwater.easybot.platforms.legacyforge.features;

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
import java.util.UUID;

//? if >=1.19 {
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
//?} else {
/*import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 *///?}

public class PlayerLoginFeature implements IEasyBotFeatures {

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 使用最高优先级，在其他模组处理加入事件前进行拦截。
     * 此时玩家已完成握手，但尚未进入世界（处于加载地形阶段）。
     * 如果在此处拦截，玩家将不会看到世界渲染，也不会出现在 Tab 列表中（取决于具体版本实现）。
     */
    //? if >=1.19 {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoin(EntityJoinLevelEvent event) {
        //?} else {
    /*@SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoin(EntityJoinWorldEvent event) {
    *///?}
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if(player.tickCount != 0) return; // 玩家首次加入世界时，tickCount 为 0

        try {
            // 执行同步检查
            boolean allow = checkLoginSync(player);
            if (!allow) {
                event.setCanceled(true);
                // 注意：disconnect 会发送断开包，客户端会显示断开连接屏幕
            }
        } catch (Exception e) {
            ModData.LOGGER.error("Error during login check", e);
            if (!ConfigLoader.get().isIgnoreError()) {
                event.setCanceled(true);
                player.connection.disconnect(Component.literal("§c服务器内部错误,请稍后再试"));
            }
        }
    }

    // 返回 true 表示允许进入，false 表示拒绝
    private boolean checkLoginSync(ServerPlayer player) {
        var profile = player.getGameProfile();
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
            return handleError(player, "当前服务器未连接到主程序");
        }

        // 获取 IP 地址
        //? if >=1.17 {
        var remoteAddress = (InetSocketAddress) player.connection.getRemoteAddress();
        //?} else {
        /*var remoteAddress = (InetSocketAddress) player.connection.netManager.getRemoteAddress();
         *///?}

        String hostName = (remoteAddress != null) ? remoteAddress.getHostName() : "unknown";

        // 异步上报信息（不阻塞，反正结果不影响登录）
        String finalName = name;
        UUID finalUuid = uuid;
        // 使用线程池异步发送“上报”请求，因为它不需要返回值
        com.springwater.easybot.threading.EasyBotNetworkingThreadPool.getInstance().addTask(() ->
                LegacyForgeEntry.getBridgeClient().reportPlayer(finalName, finalUuid.toString(), hostName), "玩家信息上报");

        try {
            var resp = LegacyForgeEntry.getBridgeClient().login(name, uuid.toString());

            if (resp.getKick()) {
                player.connection.disconnect(Component.literal(resp.getKickMessage()));
                return false;
            }
            return true;
        } catch (Exception e) {
            ModData.LOGGER.error(e.toString());
            return handleError(player, e.getLocalizedMessage());
        }
    }

    private boolean handleError(ServerPlayer player, String reason) {
        if (ConfigLoader.get().isIgnoreError()) {
            ModData.LOGGER.warn("{} (已忽略强制验证)", reason);
            return true; // 允许进入
        }
        ModData.LOGGER.error(reason);
        player.connection.disconnect(Component.literal("§c服务器内部异常,请稍后重试!"));
        return false; // 拒绝进入
    }
}
//?}
