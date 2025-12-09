package com.springwater.easybot.commands.handlers;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.commands.ICommandHandler;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BindCommandHandler implements ICommandHandler {

    // 记录正在绑定的玩家 UUID（线程安全）
    private static final Set<UUID> bindingPlayers = ConcurrentHashMap.newKeySet();

    @Override
    public void register(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack.then(
                LiteralArgumentBuilder.<CommandSourceStack>literal("bind")
                        .executes((context) -> {
                            if (!context.getSource().isPlayer()) {
                                EasyBotFabric.LOGGER.warn("温馨提示: 无法给控制台绑定账号哦。");
                                return 1;
                            }

                            var player = context.getSource().getPlayerOrException();
                            String playerName = player.getName().getString();
                            UUID uuid = player.getUUID();

                            // 检查是否已有绑定任务在进行
                            if (bindingPlayers.contains(uuid)) {
                                sendFeedback(uuid, Component.literal(
                                        ConfigLoader.get().getMessage().getBindFail()
                                                .replace("#why", "您已有绑定任务正在进行，请稍后再试")
                                                .replace("&", "§")
                                ));
                                return 1;
                            }

                            // 添加到绑定中列表
                            bindingPlayers.add(uuid);

                            EasyBotNetworkingThreadPool.getInstance().addTask(() -> {
                                try {
                                    if (!EasyBotFabric.getBridgeClient().isReady()) {
                                        sendFailAndCleanup(uuid, "当前服务器不在线");
                                        return;
                                    }

                                    var account = EasyBotFabric.getBridgeClient().getSocialAccount(playerName);
                                    if (!Objects.equals(account.getUuid(), "")) {
                                        sendFailAndCleanup(uuid, "您已经绑定了账号");
                                        return;
                                    }

                                    var pack = EasyBotFabric.getBridgeClient().startBind(playerName);
                                    sendFeedback(uuid, Component.literal(
                                            ConfigLoader.get().getMessage().getBindStart()
                                                    .replace("#code", pack.getCode())
                                                    .replace("#time", pack.getTime())
                                                    .replace("&", "§")
                                    ));
                                    bindingPlayers.remove(uuid);
                                } catch (Exception e) {
                                    EasyBotFabric.LOGGER.error("绑定任务失败: {}", e.getMessage(), e);
                                    sendFailAndCleanup(uuid, "服务器内部异常");
                                }
                            }, "绑定任务");

                            return 1;
                        })
        );
    }

    private void sendFailAndCleanup(UUID uuid, String reason) {
        bindingPlayers.remove(uuid); // 先清理
        sendFeedback(uuid, Component.literal(
                ConfigLoader.get().getMessage().getBindFail()
                        .replace("#why", reason)
                        .replace("&", "§")
        ));
    }

    private void sendFeedback(UUID uuid, Component component) {
        EasyBotFabric.getServer().execute(() -> {
            var player = EasyBotFabric.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                player.sendSystemMessage(component);
            }
        });
    }
}