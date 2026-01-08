package com.springwater.easybot.threading;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EasyBot网络请求专用线程池
 */
public class EasyBotNetworkingThreadPool {
    // 核心线程数
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    // 最大线程数
    private static final int MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    // 线程空闲超时时间（秒）
    private static final long KEEP_ALIVE_TIME = 30;
    // 线程池饱和策略：当线程池饱和时，使用调用者线程执行任务
    private static final RejectedExecutionHandler REJECTED_HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    private final ThreadPoolExecutor workerPool;
    // 用于处理延迟任务的调度器
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private volatile boolean isRunning = true;
    private static EasyBotNetworkingThreadPool instance;

    private EasyBotNetworkingThreadPool() {
        this.workerPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                r -> new Thread(r, "EasyBot-Networking-Worker-" + taskCounter.incrementAndGet() % 1000),
                REJECTED_HANDLER
        );
        // 允许核心线程超时回收，提高资源利用率
        this.workerPool.allowCoreThreadTimeOut(true);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "EasyBot-Networking-Scheduler"));

        ModData.LOGGER.info("[调度] EasyBot网络线程池已就绪，核心线程: {}, 最大线程: {}, 无等待队列", 
                CORE_POOL_SIZE, MAX_POOL_SIZE);
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
        
        boolean debugEnabled = ConfigLoader.get().isDebug();
        if (debugEnabled) {
            ModData.LOGGER.info("[调度] 任务 [{}] 已提交", fullTaskName);
        }
        
        try {
            workerPool.submit(namedTask);
        } catch (RejectedExecutionException e) {
            ModData.LOGGER.error("[调度] 线程池已满, 拒绝任务 [{}]", fullTaskName);
        }
    }

    /**
     * 立即添加高优先级任务
     */
    public void addHighPriorityTask(Runnable task, String taskName) {
        if (!isRunning) {
            ModData.LOGGER.info("管理器已停止，无法添加任务。");
            return;
        }

        // 高优先级任务直接提交，不经过队列等待
        String fullTaskName = String.format("%s-%04d", taskName, taskCounter.incrementAndGet() % 10000);
        
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
        
        boolean debugEnabled = ConfigLoader.get().isDebug();
        if (debugEnabled) {
            ModData.LOGGER.info("[调度] 高优先级任务 [{}] 已提交", fullTaskName);
        }
        
        try {
            workerPool.submit(namedTask);
        } catch (RejectedExecutionException e) {
            ModData.LOGGER.error("[调度] 线程池已满, 拒绝任务 [{}]", fullTaskName);
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

        boolean debugEnabled = ConfigLoader.get().isDebug();
        if (debugEnabled) {
            ModData.LOGGER.info("[调度] 任务 [{}] 将在 {} 秒后加入执行队列", taskName, delaySeconds);
        }

        // 使用调度器进行倒计时
        scheduler.schedule(() -> {
            if (debugEnabled) {
                ModData.LOGGER.info("[调度] 任务 [{}] 延迟结束，正在执行...", taskName);
            }
            addTask(task, taskName);
        }, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * 延迟添加高优先级任务
     */
    public void addHighPriorityTaskAfter(Runnable task, String taskName, long delaySeconds) {
        if (!isRunning) return;

        boolean debugEnabled = ConfigLoader.get().isDebug();
        if (debugEnabled) {
            ModData.LOGGER.info("[调度] 高优先级任务 [{}] 将在 {} 秒后执行", taskName, delaySeconds);
        }

        scheduler.schedule(() -> {
            if (debugEnabled) {
                ModData.LOGGER.info("[调度] 高优先级任务 [{}] 延迟结束，正在执行...", taskName);
            }
            addHighPriorityTask(task, taskName);
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        isRunning = false;
        workerPool.shutdown();
        scheduler.shutdown();
        ModData.LOGGER.info(">>> 管理器正在关闭...");
    }

    /**
     * 获取当前线程池状态信息
     */
    public String getStatusInfo() {
        return String.format("线程池状态: 活跃线程数=%d, 队列大小=%d, 已完成任务数=%d, 总任务数=%d",
                workerPool.getActiveCount(),
                workerPool.getQueue().size(),
                workerPool.getCompletedTaskCount(),
                workerPool.getTaskCount());
    }
}