package com.springwater.easybot.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Optional;

public class TextUtils {
    public static String clearStyleCode(String text) {
        return text.replaceAll("§[0-9a-fklmnorx]", "");
    }
    public static String toLegacyString(Component component) {
        StringBuilder sb = new StringBuilder();
        component.visit((style, text) -> {
            appendStyle(sb, style);
            sb.append(text).append("§r");
            return Optional.empty();
        }, Style.EMPTY);

        return sb.toString();
    }

    private static void appendStyle(StringBuilder sb, Style style) {
        if (style.getColor() != null) {
            appendRGB(sb, style.getColor().getValue());
        }
        if (style.isBold()) sb.append("§l");
        if (style.isItalic()) sb.append("§o");
        if (style.isUnderlined()) sb.append("§n");
        if (style.isStrikethrough()) sb.append("§m");
        if (style.isObfuscated()) sb.append("§k");
    }

    private static void appendRGB(StringBuilder sb, int rgb) {
        String hex = String.format("%06X", rgb & 0xFFFFFF);
        sb.append("§x");
        for (char c : hex.toCharArray()) {
            sb.append('§').append(c);
        }
    }
}
