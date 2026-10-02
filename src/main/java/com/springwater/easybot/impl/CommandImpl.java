package com.springwater.easybot.impl;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.utils.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommandImpl {

    public String DispatchCommand(String command) {
        var source = new CommandSourceImpl();
        var level = EasyBotModImpl.INSTANCE.getServer().overworld();
        //? >= 26.3 {
        /*CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS, Component.literal("EasyBotCommandDispatcher"), EasyBotModImpl.INSTANCE.getServer());
        *///?} else if >= 1.21.11 {
        CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS, "EasyBotCommandDispatcher", Component.literal("EasyBotCommandDispatcher"), EasyBotModImpl.INSTANCE.getServer(), null);
        //?} else {
        /*CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, 4, "EasyBotCommandDispatcher", Component.literal("EasyBotCommandDispatcher"), EasyBotModImpl.INSTANCE.getServer(), null);
         *///?}
        EasyBotModImpl.INSTANCE.getServer().getCommands().performPrefixedCommand(stack, command);
        var messages = source.getMessages();
        // 原有的同步实现使用换行符
        return String.join("\n", messages.stream().map(TextUtils::toLegacyString).toArray(String[]::new));
    }

    /**
     * 运行命令,支持异步结果
     * 同步执行完成后继续收集 waitTime 秒的输出，再一次性返回。
     */
    public CompletableFuture<String> DispatchCommandAsync(String command) {
        CompletableFuture<String> future = new CompletableFuture<>();
        long timeoutSeconds = Math.max(0, ConfigLoader.get().getCommand().getWaitTime());
        var source = new CommandSourceImpl();
        // 必须在主线程创建命令上下文并执行命令。
        EasyBotModImpl.INSTANCE.getServer().execute(() -> {
            try {
                var level = EasyBotModImpl.INSTANCE.getServer().overworld();
                //? >= 26.3 {
                /*CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS, Component.literal("EasyBotCommandDispatcher"), EasyBotModImpl.INSTANCE.getServer());
                *///?} else if >= 1.21.11 {
                CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS, "EasyBotCommandDispatcher", Component.literal("EasyBotCommandDispatcher"), EasyBotModImpl.INSTANCE.getServer(), null);
                //?} else {
                /*CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, 4, "EasyBotCommandDispatcher", Component.literal("EasyBotCommandDispatcher"), EasyBotModImpl.INSTANCE.getServer(), null);
                 *///?}
                EasyBotModImpl.INSTANCE.getServer().getCommands().performPrefixedCommand(stack, command);
                // ponytail: 命令 API 没有异步结束信号；仅收集窗口内输出，更长任务可调大 waitTime。
                future.completeAsync(() -> formatMessages(source),
                        CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS));
            } catch (Exception e) {
                future.completeExceptionally(e);
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
