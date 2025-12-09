package com.springwater.easybot.commands.handlers;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.springwater.easybot.commands.ICommandHandler;
import com.springwater.easybot.config.ConfigLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReloadCommandHandler implements ICommandHandler {
    // 原子布尔值确保只有一个重载任务在执行
    private static final AtomicBoolean isReloading = new AtomicBoolean(false);

    @Override
    public void register(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack.then(LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                .requires(source -> source.hasPermission(3))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (isReloading.get()) {
                        source.sendSystemMessage(
                                Component.literal("重载已在进行中，请耐心等待！").withStyle(ChatFormatting.RED)
                        );
                        return 0; // 失败返回0
                    }
                    isReloading.set(true);
                    source.sendSystemMessage(
                            Component.literal("正在后台重载配置...").withStyle(ChatFormatting.GOLD)
                    );

                    boolean isPlayer = source.isPlayer();
                    var playerUUID = isPlayer ? Objects.requireNonNull(source.getPlayer()).getUUID() : null;
                    var server = source.getServer();
                    
                    new Thread(() -> {
                        try {
                            ConfigLoader.reload(); 
                            server.execute(() -> {
                                sendReloadResult(server, playerUUID, true, null);
                            });
                        } catch (Exception e) {
                            server.execute(() -> {
                                sendReloadResult(server, playerUUID, false, e.getMessage());
                            });
                        } finally {
                            isReloading.set(false);
                        }
                    }, "EasyBot-Config-Reload-Thread").start();

                    return 1; // 成功触发返回1
                }));
    }

    /**
     * 在主线程安全地发送重载结果
     */
    private void sendReloadResult(
            MinecraftServer server,
            UUID playerUUID,
            boolean success,
            String errorMessage
    ) {
        if (success) {
            server.sendSystemMessage(
                    Component.literal("[EasyBot] 配置重载成功！").withStyle(ChatFormatting.GREEN)
            );
        } else {
            server.sendSystemMessage(
                    Component.literal("[EasyBot] 重载失败: " + errorMessage).withStyle(ChatFormatting.RED)
            );
        }
        if (playerUUID != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                if (success) {
                    player.sendSystemMessage(
                            Component.literal("配置已重载 ").withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal("[自动热重载已生效]").withStyle(ChatFormatting.GRAY))
                    );
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.0F, 1.0F);
                } else {
                    player.sendSystemMessage(
                            Component.literal("重载失败: ").withStyle(ChatFormatting.RED)
                                    .append(Component.literal(errorMessage).withStyle(ChatFormatting.YELLOW))
                    );
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.MASTER, 1.0F, 1.0F);
                }
            }
        }
    }
}