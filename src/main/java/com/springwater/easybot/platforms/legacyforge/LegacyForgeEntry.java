//? legacyforge {
package com.springwater.easybot.platforms.legacyforge;
import com.springwater.easybot.ModFlags;
import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.commands.EasyBotCommands;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.config.EasyBotConfig;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.impl.BridgeBehaviorImpl;
import com.springwater.easybot.impl.ClientProfileGetterImpl;
import com.springwater.easybot.logger.Slf4jLoggerAdapter;
import com.springwater.easybot.placeholder.PlaceholderManager;
import com.springwater.easybot.placeholder.handlers.MathHandler;
import com.springwater.easybot.placeholder.handlers.StatisticHandler;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.platforms.legacyforge.features.LoginEventSyncFeature;
import com.springwater.easybot.platforms.legacyforge.features.MessageSyncFeature;
import com.springwater.easybot.platforms.legacyforge.features.PlayerDeathSyncFeature;
import com.springwater.easybot.platforms.legacyforge.features.PlayerLoginFeature;
import com.springwater.easybot.statistic.StatisticManager;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Mod(ModData.MOD_ID)
public class LegacyForgeEntry {
    private static final Logger LOGGER = ModData.LOGGER;
    @Getter
    private static MinecraftServer server = null;

    @Getter
    private static BridgeClient bridgeClient = null;
    private static final List<IEasyBotFeatures> features = List.of(
            new LoginEventSyncFeature(),             // 消息同步接口(进入退出)
            new MessageSyncFeature(),               // 消息同步接口
            new PlayerDeathSyncFeature(),          // 玩家死亡任务(同步消息)
            new PlayerLoginFeature()
    );
    public LegacyForgeEntry() {

        LOGGER.info("EasyBot Forge-" + ModData.VERSION + "+" + ModData.MINECRAFT + " 启动中!");

        // 注册到 MinecraftForge 全局事件总线 (用于服务器事件等)
        MinecraftForge.EVENT_BUS.register(LegacyForgeEntry.class);

        try {
            ConfigLoader.load();
            ConfigLoader.registerOnConfigChanged(this::handleConfigReload);
            ConfigLoader.startAutoReloadWatcher(); // 配置文件热重载
        } catch (IOException e) {
            LOGGER.warn("=====================================");
            LOGGER.warn("配置加载失败");
            LOGGER.warn("配置路径: {}", ConfigLoader.CONFIG_PATH);
            LOGGER.warn("异常类型: {}", e.getClass().getName());
            LOGGER.error("错误信息: {}", e.getLocalizedMessage());
            LOGGER.warn("请检查您的JSON文件格式是否正确,如果您实在不确定错误原因,可以尝试删除配置文件重新进行配置");
            LOGGER.warn("=====================================");
            throw new RuntimeException("配置加载失败", e);
        }

        doDependencyCheck();

        doPlaceholderHandlerRegister();

        // 在启动前设置Logger 不用默认的
        BridgeClient.setLogger(new Slf4jLoggerAdapter(LOGGER));

        // 这一步阻塞服务器主线程, 直到配置文件中的Token填入正确
        ConfigLoader.waitForToken();

        EasyBotNetworkingThreadPool.getInstance(); // 这一步仅仅只是为了让线程池初始化资源,为后续上报任务做准备
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        doStatDbInit();
        LOGGER.info("正在获取服务器配置");
        new ClientProfileGetterImpl().BuildClientProfile(server);
        bridgeClient = new BridgeClient(ConfigLoader.get().getWs(), new BridgeBehaviorImpl());
        bridgeClient.setToken(ConfigLoader.get().getToken()); // 他会自己启动,这里不会阻塞服务器线程

        // 启动所有功能
        features.forEach(IEasyBotFeatures::register);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (EasyBotNetworkingThreadPool.getInstance() != null) {
            EasyBotNetworkingThreadPool.getInstance().shutdown(); // 先关调度器,防止bridge关了之后还发消息
        }
        if (bridgeClient != null) {
            bridgeClient.close(); // 调用这个方法bridge就真似了
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        EasyBotCommands.register(event.getDispatcher());
    }


    public void handleConfigReload(EasyBotConfig config) {
        if (server == null) return; // 大概率不会走到这里,除了用户在服务器都没创建的时候保存了配置
        LOGGER.info("正在重新获取服务器配置");
        new ClientProfileGetterImpl().BuildClientProfile(server);

        if (config.getToken().isEmpty()) {
            LOGGER.warn("您未设置Token,本次重载不生效!"); // 警告
        }

        // 在确保token设置了的情况下重启连接
        resetBridgeClient();
    }

    private static void doStatDbInit() {
        LOGGER.info("正在初始化统计数据库");
        StatisticManager.getInstance().initDb(ConfigLoader.CONFIG_PATH.getParent().resolve("uuid_mapping").toString());
        var overworld = server.getWorldPath(LevelResource.ROOT);
        var statPath = overworld.resolve("stats");
        if (!Files.exists(statPath)) {
            LOGGER.warn("未找到统计数据目录 {} 无法使用玩家统计变量!", statPath);
            ModFlags.setPlayerStatisticSupported(false);
        } else {
            StatisticManager.getInstance().setSavePath(statPath);
            ModFlags.setPlayerStatisticSupported(true);
        }
    }

    private static void doPlaceholderHandlerRegister() {
        LOGGER.info("正在注册占位符处理器");
        PlaceholderManager.getInstance().registerHandler(new StatisticHandler());
        PlaceholderManager.getInstance().registerHandler(new MathHandler());
    }

    private static void doDependencyCheck() {
        LOGGER.info("正在进行依赖检查");
        // Forge 使用 ModList 类，用法基本一致
        if (ModList.get().isLoaded("geyser_neoforge") || ModList.get().isLoaded("geyser_forge")) {
            LOGGER.info("检测到Geyser 已启动Geyser兼容功能");
            ClientProfile.setHasGeyser(true);
        }

        if (ModList.get().isLoaded("floodgate")) {
            LOGGER.info("检测到Floodgate 已启动Floodgate兼容功能");
            ClientProfile.setHasFloodgate(true);
        }
    }

    private static void resetBridgeClient() {
        if (bridgeClient != null) {
            bridgeClient.setToken(ConfigLoader.get().getToken());
            bridgeClient.resetUrl(ConfigLoader.get().getWs());
            bridgeClient.stop(); // 这里使用会自动重连的stop而不是销毁时候用的close！！！
        }
    }
}
//?}