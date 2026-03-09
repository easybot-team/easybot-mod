package com.springwater.easybot.commands.handlers;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.springwater.easybot.commands.ICommandHandler;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

public class SayCommandHandler implements ICommandHandler {

    @Override
    public void register(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("say")
                        .then(Commands.argument("messages", StringArgumentType.greedyString())
                                .executes(context -> {
                                            String message = StringArgumentType.getString(context, "messages").trim();
                                            if (Objects.equals(message, "")) {
                                                context.getSource().sendFailure(Component.literal("请输入要发送的消息").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            if (!EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                                                context.getSource().sendFailure(Component.literal("当前服务器处于离线模式").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }

                                            PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
                                            playerInfo.setIp("localhost");
                                            playerInfo.setName("CONSOLE");
                                            playerInfo.setNameRaw("CONSOLE");
                                            playerInfo.setUuid("");
                                            if (context.getSource().isPlayer()) {
                                                ServerPlayer player = context.getSource().getPlayerOrException();
                                                playerInfo = PlayerUtils.getPlayerInfo(player);
                                            }
                                            PlayerInfoWithRaw finalPlayerInfo = playerInfo;
                                            EasyBotNetworkingThreadPool.getInstance().addTask(() -> {
                                                if (EasyBotModImpl.INSTANCE.getBridgeClient().isReady()) {
                                                    EasyBotModImpl.INSTANCE.getBridgeClient().syncMessage(finalPlayerInfo, message, true);
                                                    EasyBotModImpl.INSTANCE.getServer().execute(() -> {
                                                        context.getSource().sendSuccess(() -> Component.literal("您的消息已发送: ").append(Component.literal(message).withStyle(ChatFormatting.GRAY)), true);
                                                        if (context.getSource().isPlayer()) {
                                                            ServerPlayer player = context.getSource().getPlayer();
                                                            if (player != null) {
                                                                //? >= 1.21.11 {
                                                                /*player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                                                                *///?} else {
                                                                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.0F, 1.0F);
                                                                 //?}
                                                            }
                                                        }
                                                    });
                                                }
                                            }, "消息同步-手动");
                                            return 1;
                                        }
                                )
                        )
                );
    }
}
