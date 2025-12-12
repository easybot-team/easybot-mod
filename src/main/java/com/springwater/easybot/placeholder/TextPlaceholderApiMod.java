package com.springwater.easybot.placeholder;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.ModFlags;
import com.springwater.easybot.utils.TextUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextPlaceholderApiMod {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    private static String replaceInternal(String text, String playerName) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);

        // 如果没有任何占位符，直接返回，避免 StringBuilder 开销
        if (!matcher.find()) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length() + 32);
        matcher.reset(); // 重置 matcher 状态

        while (matcher.find()) {
            String content = matcher.group(1);
            String fullKey = "%" + content + "%";
            String replacement = PlaceholderApiMappings.PLACEHOLDER_API_MAPPINGS.get(fullKey);
            if ("%player:name%".equals(replacement)) {
                replacement = playerName;
            }

            if (replacement != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String replacePlaceholders(String text, String playerName, @Nullable ServerPlayer player) {
        String intermediate = replaceInternal(text, playerName);
        if (!ModFlags.isTextPlaceholderApiInstalled()) {
            return intermediate;
        }
        try {
            var context = (player == null)
                    ? PlaceholderContext.of(EasyBotFabric.getServer())
                    : PlaceholderContext.of(player);
            var placeholders = Component.literal(intermediate);
            var result = Placeholders.parseText(placeholders, context);
            return TextUtils.toLegacyString(result);
        } catch (Exception e) {
            EasyBotFabric.LOGGER.error("TextPlaceholderApi 调用失败: {}", e.getMessage());
            return intermediate;
        }
    }
}