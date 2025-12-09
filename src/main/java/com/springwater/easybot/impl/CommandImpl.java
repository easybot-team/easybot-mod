package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.utils.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandImpl {

    public String DispatchCommand(String command) {
        var source = new CommandSourceImpl();
        var level = EasyBotFabric.getServer().overworld();
        CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, 4, "EasyBotCommandDispatcher", Component.literal("EasyBotCommandDispatcher"), EasyBotFabric.getServer(), null);
        EasyBotFabric.getServer().getCommands().performPrefixedCommand(stack, command);
        var messages = source.getMessages();
        // 原有的同步实现使用换行符
        return String.join("\n", messages.stream().map(TextUtils::toLegacyString).toArray(String[]::new));
    }

    /**
     * 运行命令,支持异步结果
     * 如果消息是0条 就一直等待到有一条消息 直到超时
     */
    public CompletableFuture<String> DispatchCommandAsync(String command) {
        CompletableFuture<String> future = new CompletableFuture<>();
        long timeoutSeconds = ConfigLoader.get().getCommand().getWaitTime();
        AtomicBoolean syncExecutionFinished = new AtomicBoolean(false);
        var source = new CommandSourceImpl() {
            @Override
            public void sendSystemMessage(Component component) {
                super.sendSystemMessage(component);
                // 只有当同步执行阶段已经结束，且Future尚未完成时，才视为异步消息到达并触发完成
                if (syncExecutionFinished.get() && !future.isDone()) {
                    future.complete(formatMessages(this));
                }
            }
        };

        var level = EasyBotFabric.getServer().overworld();
        CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, 4, "EasyBotCommandDispatcher", Component.literal("EasyBotCommandDispatcher"), EasyBotFabric.getServer(), null);

        // 必须在主线程执行
        EasyBotFabric.getServer().execute(() -> {
            try {
                EasyBotFabric.getServer().getCommands().performPrefixedCommand(stack, command);
                // 标记同步执行结束
                syncExecutionFinished.set(true);
                // 如果同步执行后已经有消息了，直接完成，不需要等待异步回调
                if (!source.getMessages().isEmpty()) {
                    future.complete(formatMessages(source));
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        // 理超时逻辑 (异步等待)
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(timeoutSeconds);
                // 如果超时后 Future 仍未完成（说明没有等到异步消息），则强制完成并返回当前收集到的所有内容（可能是空）
                if (!future.isDone()) {
                    future.complete(formatMessages(source));
                }
            } catch (InterruptedException e) {
                // 忽略中断
            }
        });

        return future;
    }
    
    private String formatMessages(CommandSourceImpl source) {
        return String.join(",", source.getMessages().stream()
                .map(TextUtils::toLegacyString)
                .toArray(String[]::new));
    }
}