package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.message.TextSegment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.placeholder.PlaceholderApi;
import com.springwater.easybot.utils.LoaderUtils;
import com.springwater.easybot.utils.PlayerInfoUtils;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BridgeBehaviorImpl implements BridgeBehavior {
    private static final CommandImpl COMMAND_IMPL = new CommandImpl();

    @Override
    public String runCommand(String playerName, String command, boolean enableRcon) {
        return COMMAND_IMPL.DispatchCommand(command);
    }

    @Override
    public String papiQuery(String playerName, String query) {
        var future = new CompletableFuture<String>();
        EasyBotFabric.getServer().execute(() -> {
            try {
                var player = EasyBotFabric.getServer().getPlayerList().getPlayerByName(playerName);
                var resp = PlaceholderApi.replacePlaceholders(query, playerName, player);
                future.complete(resp);
            } catch (Exception e) {
                EasyBotFabric.LOGGER.error("PAPI查询失败: {}", e.getMessage());
                future.completeExceptionally(e);
            }
        });
        return future.join();
    }

    @Override
    public ServerInfo getInfo() {
        // 主程序真正获取服务器信息的地方
        ServerInfo info = new ServerInfo();
        info.setServerName(LoaderUtils.isQuilt() ? "Quilt" : "Fabric");
        info.setServerVersion(EasyBotFabric.getServer().getServerVersion());
        info.setPluginVersion(EasyBotFabric.VERSION);
        info.setCommandSupported(ClientProfile.isCommandSupported());
        info.setPapiSupported(ClientProfile.isPapiSupported());
        info.setHasGeyser(ClientProfile.isHasGeyser());
        info.setOnlineMode(ClientProfile.isOnlineMode());
        return info;
    }

    @Override
    public void SyncToChat(String message) {
        EasyBotFabric.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    @Override
    public void BindSuccessBroadcast(String playerName, String accountId, String accountName) {
        EasyBotFabric.getServer().execute(() -> {
            var isDebug = ConfigLoader.get().isDebug();
            if (!EasyBotFabric.getBridgeClient().isReady()) {
                // 能走到这里也是十分甚至九分奇怪了, 能在线收到绑定成功消息,但是处理的时候却不在线了,就很奇怪咯
                EasyBotFabric.LOGGER.warn("玩家{}绑定账号{}({})成功,但当前服务器在处理时掉线 (本次处理为离线处理)", playerName, accountId, accountName);
            }


            if (isDebug) {
                EasyBotFabric.LOGGER.info("收到通知: {}绑定账号{}({})成功", playerName, accountId, accountName);
            }

            var bindPlayer = EasyBotFabric.getServer().getPlayerList().getPlayerByName(playerName);
            if (bindPlayer != null) {
                bindPlayer.playNotifySound(
                        SoundEvents.PLAYER_LEVELUP,
                        SoundSource.MASTER,
                        1.0f,
                        1.0f
                );
                // 通知绑定成功的喜报!!
                bindPlayer.sendSystemMessage(
                        Component.literal(
                                ConfigLoader.get().getMessage().getBindSuccess()
                                        .replace("&", "§")
                                        .replace("#player", playerName)
                                        .replace("#account", accountId)
                                        .replace("#name", accountName)
                        )
                );
            } else if (isDebug) {
                EasyBotFabric.LOGGER.warn("玩家{}绑定账号{}({})成功,但玩家不在线 (跳过通知)", playerName, accountId, accountName);
            }


            if (!ConfigLoader.get().getEvent().isEnableSuccessEvent()) {
                if (isDebug) {
                    EasyBotFabric.LOGGER.warn("玩家{}绑定账号{}({})成功,本服未开启命令执行 跳过流程", playerName, accountId, accountName);
                }
                return;
            }

            if (ConfigLoader.get().getEvent().isEnableSuccessEvent()) {
                var commands = ConfigLoader.get().getEvent().getBindSuccess();
                if (isDebug) {
                    EasyBotFabric.LOGGER.info("玩家{}绑定账号{}({})成功,正在执行命令: {}条", playerName, accountId, accountName, commands.toArray().length);
                }
                for (String command : commands) {
                    COMMAND_IMPL.DispatchCommand(command.replace("&", "§").replace("$player", playerName).replace("$account", accountId).replace("$name", accountName));
                }
            }


        });
    }

    @Override
    public void KickPlayer(String playerName, String reason) {
        PlayerUtils.kickPlayerAsync(playerName, reason); // 这里调用多线程版本的踢出玩家 (走这里的基本上是解绑踢出)
    }

    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        List<Segment> segmentsToAdd = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();

        // 需要将连在一起的纯文本字段合并到一起 别问，干就完了
        for (Segment segment : segments) {
            if (segment instanceof TextSegment) {
                currentText.append(segment.getText());
            } else {
                if (!currentText.isEmpty()) {
                    TextSegment combinedTextSegment = new TextSegment();
                    combinedTextSegment.setText(currentText.toString());
                    segmentsToAdd.add(combinedTextSegment);
                    // 清空缓冲区
                    currentText.setLength(0);
                }
                // 直接添加这个非文本片段
                segmentsToAdd.add(segment);
            }
        }

        // 循环结束后，处理缓冲区里剩余的文本
        if (!currentText.isEmpty()) {
            TextSegment combinedTextSegment = new TextSegment();
            combinedTextSegment.setText(currentText.toString());
            segmentsToAdd.add(combinedTextSegment);
        }
        MutableComponent root = ComponentBuilderImpl.build(segmentsToAdd);
        EasyBotFabric.getServer().getPlayerList().broadcastSystemMessage(root, false);
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        return PlayerInfoUtils.buildPlayerInfoList(EasyBotFabric.getServer().getPlayerList().getPlayers());
    }
}
