package com.springwater.easybot;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.config.EasyBotConfig;
import com.springwater.easybot.features.*;
import com.springwater.easybot.impl.BridgeBehaviorImpl;
import com.springwater.easybot.impl.ClientProfileGetterImpl;
import com.springwater.easybot.logger.Slf4jLoggerAdapter;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

//
// 给开发人员的文档,你可能注意到了代码中的 /*$ mod_id*/ 类似的注释
// 这其实是模板代码, 运行时替换为实际值, 我们基于stonecutter进行开发, 关于这部分内容请参考文档
// https://stonecutter.kikugie.dev/wiki/faq
//

/**
 * EasyBotFabric插件实例
 */
public class EasyBotFabric implements ModInitializer {
    public static final String MOD_ID = /*$ mod_id*/ "easybotfabric"; // 别删前面的注释,删了我打屎你们
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.1.0"; // 别删前面的注释,删了我打屎你们
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.10";   // 别删前面的注释,删了我打屎你们
    public static final String PAPI = /*$ papi*/ "";   // 别删前面的注释,删了我打屎你们

    @Getter
    private static MinecraftServer server = null;

    @Getter
    private static BridgeClient bridgeClient = null;

    private static final List<IEasyBotFeatures> features = List.of(
            new PlayerLoginFeature(),               // 玩家登陆任务(强制绑定触发器)
            new LoginEventSyncFeature(),             // 消息同步接口(进入退出)
            new MessageSyncFeature(),             // 消息同步接口
            new PlayerDeathSyncFeature()             // 玩家死亡任务(同步消息)
    );

    @Override
    public void onInitialize() throws RuntimeException {
        // 例 EasyBot Fabric-1.0.0+1.20.1 启动中!
        LOGGER.info("EasyBot Fabric-" + VERSION + "+" + MINECRAFT + " 启动中!");
        try {
            ConfigLoader.load();
            ConfigLoader.registerOnConfigChanged(EasyBotFabric::handleConfigReload);
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

        // 在启动前设置Logger 不用默认的
        BridgeClient.setLogger(new Slf4jLoggerAdapter(LOGGER));

        // 这一步阻塞服务器主线程, 直到配置文件中的Token填入正确
        ConfigLoader.waitForToken();

        EasyBotNetworkingThreadPool.getInstance(); // 这一步仅仅只是为了让线程池初始化资源,为后续上报任务做准备

        // 启动所有功能
        features.forEach(IEasyBotFeatures::register);

        // 用FabricApi获取服务器实例比MinecraftClient.getInstance().getServer()更有兼容性
        // 开发时注意: 尽量调用FabricApi
        ServerLifecycleEvents.SERVER_STARTING.register(serverInstance -> {
            server = serverInstance;
            LOGGER.info("正在获取服务器配置");
            new ClientProfileGetterImpl().BuildClientProfile(serverInstance);
            bridgeClient = new BridgeClient(ConfigLoader.get().getWs(), new BridgeBehaviorImpl());
            bridgeClient.setToken(ConfigLoader.get().getToken()); // 他会自己启动,这里不会阻塞服务器线程
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(serverInstance -> {
            EasyBotNetworkingThreadPool.getInstance().shutdown(); // 先关调度器,防止bridge关了之后还发消息
            bridgeClient.close(); // 调用这个方法bridge就真似了
        });
    }

    public static void handleConfigReload(EasyBotConfig config) {
        if (server == null) return; // 大概率不会走到这里,除了用户在服务器都没创建的时候保存了配置, 要是其他情况能走到这里我得制裁写代码的人了
        LOGGER.info("正在重新获取服务器配置");
        new ClientProfileGetterImpl().BuildClientProfile(server);

        // 考虑到服务器已经成功运行了,这里如果没设置token就没设置吧..
        // 卡住运行中的服务器线程 那服主很头大了
        // ConfigLoader.waitForToken();

        if (config.getToken().isEmpty()) {
            LOGGER.warn("您未设置Token,本次重载不生效!"); // 警告
        }

        // 在确保token设置了的情况下重启连接
        resetBridgeClient();
    }

    private static void doDependencyCheck() {
        LOGGER.info("正在进行依赖检查");
        if(FabricLoader.getInstance().isModLoaded("placeholder-api")){
            LOGGER.info("检测到TextPlaceholderAPI 已启动占位符功能");
            ClientProfile.setPapiSupported(true);
        }else{
            LOGGER.warn("未检测到TextPlaceholderAPI 为了您更好的使用体验,建议您安装此MOD: https://modrinth.com/mod/placeholder-api");
            ClientProfile.setPapiSupported(false);
        }
    }
    
    private static void resetBridgeClient() {
        bridgeClient.setToken(ConfigLoader.get().getToken());
        bridgeClient.resetUrl(ConfigLoader.get().getWs());
        bridgeClient.stop(); // 这里使用会自动重连的stop而不是销毁时候用的close！！！
    }
}