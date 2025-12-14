//? neoforge {
package com.springwater.easybot.platforms.neoforge.features;

import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.statistic.StatisticManager;
import com.springwater.easybot.utils.FloodgateUtils;
import com.springwater.easybot.utils.GameProfileUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

//? >1.20.4 {
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;
 //?}

//? <=1.20.4 {
/*import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
*///?}


public class PlayerLoginFeature implements IEasyBotFeatures {

    @Override
    public void register() {
    }

    //? <=1.20.4 {
    /*@SubscribeEvent
    public void onRegisterConfigurationTasks(OnGameConfigurationEvent event) {
        event.register(new EasyBotLoginCheckTask((ServerConfigurationPacketListenerImpl) event.getListener()));
    }
    public record EasyBotLoginCheckTask(ServerConfigurationPacketListenerImpl listener) implements ICustomConfigurationTask {
        public static final Type TYPE = new Type(ModData.MOD_ID + ":" + "login_check"); // 有更加标准的实现方法,但是我不想做跨版本兼容 我不想去搜索

        @Override
        public void run(@NotNull Consumer<CustomPacketPayload> consumer) {
            CompletableFuture.runAsync(() -> {
                try {
                    checkLogin();
                    listener.finishCurrentTask(TYPE);
                } catch (Exception e) {
                    ModData.LOGGER.error("Error during login check", e);
                    listener.disconnect(Component.literal("§c服务器内部错误,请稍后再试"));
                }
            });
        }

        private void checkLogin() {
            var profile = listener.getOwner(); // 1.21+ 可以直接获取 GameProfile
            var name = GameProfileUtils.getName(profile);
            var uuid = UUID.fromString(GameProfileUtils.getUuid(profile));

            PlayerInfo floodgatePlayer = FloodgateUtils.getFloodgatePlayerInfo(uuid);
            if (floodgatePlayer != null) {
                name = floodgatePlayer.getPlayerName();
                uuid = UUID.fromString(floodgatePlayer.getPlayerUuid());
            }
            StatisticManager.getInstance().getStatDb().putUuidCache(name, uuid);

            if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                handleError("当前服务器未连接到主程序");
                return;
            }

            var remoteAddress = (InetSocketAddress) listener.getConnection().getRemoteAddress();
            String hostName = remoteAddress.getHostName();

            EasyBotModImpl.INSTANCE.getBridgeClient().reportPlayer(name, uuid.toString(), hostName);

            try {
                var resp = EasyBotModImpl.INSTANCE.getBridgeClient().login(name, uuid.toString());
                if (resp.getKick()) {
                    // 在配置阶段断开连接
                    listener.disconnect(Component.literal(resp.getKickMessage()));
                }
            } catch (Exception e) {
                handleError(e.getLocalizedMessage());
                ModData.LOGGER.error(e.toString());
            }
        }

        private void handleError(String reason) {
            if (ConfigLoader.get().isIgnoreError()) {
                ModData.LOGGER.warn("{} (已忽略强制验证)", reason);
                return;
            }
            ModData.LOGGER.error(reason);
            listener.disconnect(Component.literal("§c服务器内部异常,请稍后重试!"));
        }

        @Override
        public @NotNull Type type() {
            return TYPE;
        }
    }
    *///?}
    
    
    //? >1.20.4 {
    
    @SubscribeEvent
    public void onRegisterConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new EasyBotLoginCheckTask((ServerConfigurationPacketListenerImpl) event.getListener()));
    }
    public record EasyBotLoginCheckTask(ServerConfigurationPacketListenerImpl listener) implements ConfigurationTask {
        public static final Type TYPE = new Type(ModData.MOD_ID + ":" + "login_check"); // 有更加标准的实现方法,但是我不想做跨版本兼容 我不想去搜索

        @Override
        public void start(@NotNull Consumer<Packet<?>> consumer) {
            CompletableFuture.runAsync(() -> {
                try {
                    checkLogin();
                    listener.finishCurrentTask(TYPE);
                } catch (Exception e) {
                    ModData.LOGGER.error("Error during login check", e);
                    listener.disconnect(Component.literal("§c服务器内部错误,请稍后再试"));
                }
            });
        }

        private void checkLogin() {
            var profile = listener.getOwner(); // 1.21+ 可以直接获取 GameProfile
            var name = GameProfileUtils.getName(profile);
            var uuid = UUID.fromString(GameProfileUtils.getUuid(profile));

            PlayerInfo floodgatePlayer = FloodgateUtils.getFloodgatePlayerInfo(uuid);
            if (floodgatePlayer != null) {
                name = floodgatePlayer.getPlayerName();
                uuid = UUID.fromString(floodgatePlayer.getPlayerUuid());
            }
            StatisticManager.getInstance().getStatDb().putUuidCache(name, uuid);

            if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                handleError("当前服务器未连接到主程序");
                return;
            }

            var remoteAddress = (InetSocketAddress) listener.getConnection().getRemoteAddress();
            String hostName = remoteAddress.getHostName();

            EasyBotModImpl.INSTANCE.getBridgeClient().reportPlayer(name, uuid.toString(), hostName);

            try {
                var resp = EasyBotModImpl.INSTANCE.getBridgeClient().login(name, uuid.toString());
                if (resp.getKick()) {
                    // 在配置阶段断开连接
                    listener.disconnect(Component.literal(resp.getKickMessage()));
                }
            } catch (Exception e) {
                handleError(e.getLocalizedMessage());
                ModData.LOGGER.error(e.toString());
            }
        }

        private void handleError(String reason) {
            if (ConfigLoader.get().isIgnoreError()) {
                ModData.LOGGER.warn("{} (已忽略强制验证)", reason);
                return;
            }
            ModData.LOGGER.error(reason);
            listener.disconnect(Component.literal("§c服务器内部异常,请稍后重试!"));
        }

        @Override
        public @NotNull Type type() {
            return TYPE;
        }
    }
    //?}
}
//?}