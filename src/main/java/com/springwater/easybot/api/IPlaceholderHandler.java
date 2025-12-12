package com.springwater.easybot.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface IPlaceholderHandler {
    String getPrefix();

    @Nullable String replacePlaceholders(String argumentWithoutPrefix, String playerName, @Nullable ServerPlayer player);

    @Nullable String replacePlaceholders(String argumentWithoutPrefix, String playerName, MinecraftServer server);
}
