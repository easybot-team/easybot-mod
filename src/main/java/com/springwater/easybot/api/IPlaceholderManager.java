package com.springwater.easybot.api;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface IPlaceholderManager {
    void registerHandler(IPlaceholderHandler handler);
    String replacePlaceholders(String text, String playerName, @Nullable ServerPlayer player);
}
