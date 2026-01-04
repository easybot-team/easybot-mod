package com.springwater.easybot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 负责管理 EasyBotConfig 的加载、保存和热重载逻辑。
 */
public class ConfigLoader {
    private static final Logger LOGGER = ModData.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String CONFIG_ROOT_PATH = EasyBotModImpl.INSTANCE.getConfigDirectory().resolve(ModData.MOD_ID).toString();
    public static final Path CONFIG_PATH = Path.of(CONFIG_ROOT_PATH, CONFIG_FILE_NAME);
    private static final AtomicBoolean isWatcherRunning = new AtomicBoolean(false);

    // 当前正在使用的配置实例
    private static EasyBotConfig currentConfig;

    // 配置变更监听器列表（线程安全）
    private static final List<Consumer<EasyBotConfig>> listeners = new CopyOnWriteArrayList<>();
    
    // 文件内容哈希缓存，用于避免不必要的配置重载
    private static long lastFileHash = 0;

    /**
     * 获取当前配置实例。如果尚未加载会直接报错
     */
    public static EasyBotConfig get() {
        if (currentConfig == null) {
            // 这种情况还是比较罕见的,currentConfig == null的情况只有初始化失败
            // 初始化失败一般情况下会直接崩溃、而不会走到这里
            throw new IllegalStateException("未初始化配置！");
        }
        return currentConfig;
    }

    /**
     * 阻塞当前线程，直到配置文件中填入了有效的 Token。
     * 只有当文件被修改并保存时，才会唤醒线程进行检查。
     */
    public static void waitForToken() {
        if (isValidToken(currentConfig)) {
            return;
        }

        LOGGER.warn("检测到 Token 为空！");
        LOGGER.warn("请打开配置文件: {}", CONFIG_PATH.toAbsolutePath());
        LOGGER.warn("填入 Token 并保存文件，程序将自动继续运行...");
        LOGGER.warn("配置教程: https://docs.inectar.cn/docs/easybot/quick_start/plugin/mod/install_mod");

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Path parentDir = CONFIG_PATH.getParent();
            parentDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                // 直到文件夹内有文件发生变化才会唤醒
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待 Token 时被中断", x);
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // 忽略溢出事件
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // 获取变化的文件名
                    @SuppressWarnings("unchecked") WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    // 5. 确认变化的是我们的配置文件
                    if (filename.toString().equals(CONFIG_FILE_NAME)) {
                        LOGGER.info("检测到配置文件变化，正在尝试读取...");

                        // 尝试重新加载配置
                        // 注意：文件保存瞬间可能由于系统锁导致读取失败，reload() 内部已处理异常
                        // 有时编辑器保存会触发多次事件... 我管你这那的 reload 只是多读几次
                        reload();

                        if (isValidToken(currentConfig)) {
                            LOGGER.info("成功获取 Token！继续运行...");
                            return;
                        } else {
                            LOGGER.warn("配置文件已更新，但 Token 依然为空或无效，请重新检查。");
                        }
                    }
                }

                // 重置 key，如果返回 false 说明目录已不可访问（如被删除），跳出循环
                boolean valid = key.reset();
                if (!valid) {
                    throw new RuntimeException("配置文件目录不再有效，无法继续监听。");
                }
            }
        } catch (IOException e) {
            LOGGER.error("文件监听器启动失败", e);
            throw new RuntimeException("无法监听配置文件变化", e);
        }
    }

    /**
     * 辅助方法：判断配置中的 Token 是否有效
     */
    private static boolean isValidToken(EasyBotConfig config) {
        return config != null && config.getToken() != null && !config.getToken().trim().isEmpty() && !config.getToken().equals("在此处填写Token"); // 防止用户没改默认值
    }

    /**
     * 首次加载配置。如果文件不存在，创建默认文件。
     *
     * @throws IOException         读取配置文件时发生 IO 错误
     * @throws JsonSyntaxException 配置文件格式错误
     */
    public static void load() throws IOException {
        if (!Files.exists(CONFIG_PATH)) {
            LOGGER.info("未找到配置文件，正在创建默认配置...");
            createDefaultConfigFromResources();
        }

        String content = Files.readString(CONFIG_PATH);
        currentConfig = GSON.fromJson(content, EasyBotConfig.class);
        lastFileHash = content.hashCode(); // 初始化文件哈希缓存
        LOGGER.info("配置加载成功。");
    }

    /**
     * 热重载配置
     */
    public static void reload() {
        LOGGER.info("正在热重载配置...");
        if (!Files.exists(CONFIG_PATH)) {
            LOGGER.warn("配置文件不存在，尝试重新创建...");
            createDefaultConfigFromResources();
        }

        try {
            String content = Files.readString(CONFIG_PATH);
            long currentHash = content.hashCode();
            
            // 检查文件内容是否变化，避免不必要的重载
            if (currentHash == lastFileHash) {
                LOGGER.debug("配置文件内容未变化，跳过重载");
                return;
            }
            
            EasyBotConfig newConfig = GSON.fromJson(content, EasyBotConfig.class);
            if (newConfig == null) {
                throw new JsonSyntaxException("解析结果为 null");
            }
            currentConfig = newConfig;
            lastFileHash = currentHash; // 更新哈希缓存
            LOGGER.info("配置热重载成功！");
            notifyListeners();
        } catch (JsonSyntaxException e) {
            LOGGER.error("============================================================");
            LOGGER.error("!!! 配置重载失败 (JSON 语法错误) !!!");
            LOGGER.error("!!! 系统将继续使用【上一个可用版本】配置");
            LOGGER.error("!!! 错误详情: {}", e.getMessage());
            LOGGER.error("============================================================");
        } catch (IOException e) {
            LOGGER.error("读取配置文件时发生 IO 错误，配置未更新。", e);
        }
    }

    /**
     * 启动自动重载看门狗线程。
     */
    public static void startAutoReloadWatcher() {
        if (isWatcherRunning.getAndSet(true)) {
            LOGGER.warn("配置自动重载监听器已在运行，无需重复启动。");
            return;
        }

        Thread watcherThread = new Thread(() -> {
            LOGGER.info("启动配置自动热重载监听器...");
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                Path parentDir = CONFIG_PATH.getParent();
                // 确保目录存在
                if (!Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }

                parentDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watcher.take(); // 阻塞等待事件
                    } catch (InterruptedException x) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    boolean needsReload = false;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                        @SuppressWarnings("unchecked") WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        if (filename.toString().equals(CONFIG_FILE_NAME)) {
                            needsReload = true;
                        }
                    }

                    if (needsReload) {
                        LOGGER.info("检测到配置文件变化，准备热重载...");
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }

                        // 清空队列中在 sleep 期间产生的多余事件，避免重复 reload
                        List<WatchEvent<?>> pollEvents = key.pollEvents();
                        if (!pollEvents.isEmpty()) {
                            LOGGER.debug("合并了额外 {} 个文件变更事件", pollEvents.size());
                        }

                        // 执行重载
                        reload();
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        LOGGER.error("配置文件目录不可访问，热重载监听器停止。");
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("配置监听器发生错误", e);
            } finally {
                isWatcherRunning.set(false);
            }
        }, "EasyBot-Config-Watcher");

        // 设置为守护线程，这样当游戏/服务器关闭时，这个线程会自动结束，不会阻止 JVM 退出
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    /**
     * 保存当前内存中的配置到磁盘。
     */
    public static void save() {
        if (currentConfig == null) {
            currentConfig = new EasyBotConfig();
        }
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(currentConfig));
            LOGGER.info("配置已保存。");
        } catch (IOException e) {
            LOGGER.error("保存配置失败!", e);
        }
    }

    /**
     * 注册当配置发生变化（重载成功）时的回调函数。
     *
     * @param listener 接收新配置的 Consumer
     */
    public static void registerOnConfigChanged(Consumer<EasyBotConfig> listener) {
        listeners.add(listener);
    }

    private static void notifyListeners() {
        for (Consumer<EasyBotConfig> listener : listeners) {
            try {
                listener.accept(currentConfig);
            } catch (Exception e) {
                LOGGER.error("执行配置变更回调时发生异常", e);
            }
        }
    }

    private static void createDefaultConfigFromResources() {
        try (InputStream stream = ConfigLoader.class.getResourceAsStream(CONFIG_FILE_NAME)) {
            if (stream == null) {
                LOGGER.warn("未找到默认配置文件，将使用内置配置。");
                currentConfig = new EasyBotConfig();
                save();
            } else {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.copy(stream, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("从 Jar 复制默认配置失败。", e);
        }
    }
}