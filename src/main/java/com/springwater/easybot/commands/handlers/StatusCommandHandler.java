package com.springwater.easybot.commands.handlers;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.commands.ICommandHandler;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class StatusCommandHandler implements ICommandHandler {

    @Override
    public void register(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack.then(LiteralArgumentBuilder.<CommandSourceStack>literal("status").requires((source) -> source.hasPermission(3)).executes((context) -> {
            CommandSourceStack source = context.getSource();
            MutableComponent root = Component.empty();


            root.append(Component.literal("-------------------------------------------------------------")
                    .withStyle(ChatFormatting.GREEN));

            root.append(Component.literal("\n-> EasyBot Fabric V" + ModData.VERSION)
                    .withStyle(ChatFormatting.GRAY)
                    .withStyle(ChatFormatting.BOLD)); // 可选加粗
            root.append(Component.literal("\n构建信息: MCVersion=")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ModData.MINECRAFT)
                            .withStyle(ChatFormatting.DARK_GRAY)));
            root.append(Component.literal("\n当前状态: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(EasyBotModImpl.INSTANCE.getBridgeClient().isReady() ? "已连接" : "离线")
                            .withStyle(EasyBotModImpl.INSTANCE.getBridgeClient().isReady() ? ChatFormatting.GREEN : ChatFormatting.RED)));
            MutableComponent papiLine = Component.literal("\n依赖[TextPlaceholderAPI] - ")
                    .withStyle(ChatFormatting.GRAY);
            if (ClientProfile.isPapiSupported()) {
                papiLine.append(Component.literal("已安装 ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("[API版本: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(ModData.PAPI)
                                        .withStyle(ChatFormatting.AQUA))
                                .append(Component.literal("]"))
                        ));
            } else {
                papiLine.append(Component.literal("未安装").withStyle(ChatFormatting.RED));
            }
            root.append(papiLine);
            root.append(Component.literal("\n依赖[Geyser] - ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ClientProfile.isHasGeyser() ? "已安装" : "未安装")
                            .withStyle(ClientProfile.isHasGeyser() ? ChatFormatting.GREEN : ChatFormatting.RED)));
            root.append(Component.literal("\n依赖[Floodgate] - ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ClientProfile.isHasFloodgate() ? "已安装" : "未安装")
                            .withStyle(ClientProfile.isHasFloodgate() ? ChatFormatting.GREEN : ChatFormatting.RED)));
            root.append(Component.literal("\n-------------------------------------------------------------")
                    .withStyle(ChatFormatting.GREEN));

            if (context.getSource().isPlayer()) {
                ServerPlayer player = context.getSource().getPlayer();
                if (player != null) {
                    if (EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.0F, 1.0F);
                    } else {
                        player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.MASTER, 1.0F, 1.0F);
                    }
                }
            }

            source.sendSuccess(() -> root, false);
            return 1;
        }));
    }
}
