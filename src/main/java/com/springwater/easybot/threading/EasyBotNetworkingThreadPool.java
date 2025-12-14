package com.springwater.easybot.threading;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EasyBot网络请求专用线程池
 */
public class EasyBotNetworkingThreadPool {
    private final BlockingQueue<Runnable> taskQueue;
    private final ExecutorService workerPool;
    // 新增：用于处理延迟任务的调度器
    private final ScheduledExecutorService scheduler;
    private final Thread managerThread;
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private volatile boolean isRunning = true;
    private static EasyBotNetworkingThreadPool instance;

    private EasyBotNetworkingThreadPool() {
        this.taskQueue = new LinkedBlockingQueue<>();
        // 3个工作线程
        this.workerPool = Executors.newFixedThreadPool(3);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "EasyBot-Networking-Scheduler"));

        this.managerThread = new Thread(this::dispatchLoop, "EasyBot-NetworkingThreadPool-Manager");
        this.managerThread.start();
        ModData.LOGGER.info("[调度] EasyBot网络线程池已就绪");
    }

    public static synchronized EasyBotNetworkingThreadPool getInstance() {
        if (instance == null) {
            instance = new EasyBotNetworkingThreadPool();
        }
        return instance;
    }

    /**
     * 立即添加任务
     */
    public void addTask(Runnable task, String taskName) {
        if (!isRunning) {
            ModData.LOGGER.info("管理器已停止，无法添加任务。");
            return;
        }

        // 生成带四位编号的任务名 (例如: Download-0001)
        String fullTaskName = String.format("%s-%04d", taskName, taskCounter.incrementAndGet() % 10000);

        // 包装任务以携带名称
        Runnable namedTask = new Runnable() {
            @Override
            public void run() {
                task.run();
            }

            @Override
            public String toString() {
                return fullTaskName;
            }
        };
        if (ConfigLoader.get().isDebug()) {
            ModData.LOGGER.info("[管理器] 任务 [{}] 已加入队列 (排队数: {})", fullTaskName, taskQueue.size());
        }
        if (!taskQueue.offer(namedTask)) {
            // 能跑到这里的也是神人环境了
            ModData.LOGGER.error("[管理器] 队列已满, 放弃任务 [{}]", fullTaskName);
        }
    }

    /**
     * 延迟添加任务
     *
     * @param task         任务逻辑
     * @param taskName     任务名称前缀
     * @param delaySeconds 延迟秒数
     */
    public void addTaskAfter(Runnable task, String taskName, long delaySeconds) {
        if (!isRunning) return;

        if (ConfigLoader.get().isDebug()) {
            ModData.LOGGER.info("[调度] 任务 [{}] 将在 {} 秒后加入执行队列", taskName, delaySeconds);
        }

        // 使用调度器进行倒计时
        scheduler.schedule(() -> {
            if (ConfigLoader.get().isDebug()) {
                ModData.LOGGER.info("[调度] 任务 [{}] 延迟结束，正在加入主队列...", taskName);
            }
            addTask(task, taskName);
        }, delaySeconds, TimeUnit.SECONDS);
    }

    private void dispatchLoop() {
        while (isRunning || !taskQueue.isEmpty()) {
            try {
                Runnable task = taskQueue.take();
                if (ConfigLoader.get().isDebug()) {
                    ModData.LOGGER.info("[调度] 正在分发任务 [{}] 到线程池...", task);
                }
                workerPool.submit(task);
            } catch (InterruptedException e) {
                ModData.LOGGER.error("管理线程中断，退出...");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                ModData.LOGGER.error("管理线程异常: {}", e.getLocalizedMessage());
                ModData.LOGGER.error(e.toString());
            }
        }
    }

    public void shutdown() {
        isRunning = false;
        managerThread.interrupt();
        workerPool.shutdown();
        scheduler.shutdown();
        ModData.LOGGER.info(">>> 管理器正在关闭...");
    }
}