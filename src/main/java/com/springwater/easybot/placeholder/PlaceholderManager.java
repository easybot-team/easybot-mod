package com.springwater.easybot.placeholder;

import com.springwater.easybot.api.IPlaceholderHandler;
import com.springwater.easybot.api.IPlaceholderManager;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager implements IPlaceholderManager {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    @Getter
    private static final IPlaceholderManager instance = new PlaceholderManager();
    private final Map<String, IPlaceholderHandler> handlers = new ConcurrentHashMap<>();

    private PlaceholderManager() {
    }

    @Override
    public void registerHandler(IPlaceholderHandler handler) {
        if (handlers.putIfAbsent(handler.getPrefix(), handler) != null) {
            throw new IllegalArgumentException("内部已存在使用此前缀的处理器: '" + handler.getPrefix());
        }
    }
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
            if ("%player:name%".equals(replacement) || "%player_name%".equals(replacement)) {
                replacement = playerName;
            }

            if (replacement != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String replacePlaceholders(String text, String playerName, @Nullable ServerPlayer player) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String intermediate = replaceInternal(text, playerName);
        //? fabric {
        intermediate = TextPlaceholderApiMod.replacePlaceholders(intermediate, player);
        //?}
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(intermediate);
        if (!matcher.find()) {
            return intermediate;
        }
        matcher.reset();
        StringBuilder sb = new StringBuilder(intermediate.length() + 32);
        while (matcher.find()) {
            String fullKey = matcher.group(1); // 获取 %中间的内容%
            String replacement = null;
            int separatorIndex = fullKey.indexOf('_');
            String mainKey;
            String args;

            if (separatorIndex != -1) {
                mainKey = fullKey.substring(0, separatorIndex);
                args = fullKey.substring(separatorIndex + 1); // 自动跳过 '_'
            } else {
                mainKey = fullKey;
                args = "";
            }
            IPlaceholderHandler handler = handlers.get(mainKey);
            if (handler != null) {
                try {
                    if (player == null) {
                        var server = EasyBotModImpl.INSTANCE.getServer();
                        replacement = handler.replacePlaceholders(args, playerName, server);
                    } else {
                        replacement = handler.replacePlaceholders(args, playerName, player);
                    }
                } catch (Exception e) {
                    ModData.LOGGER.error("PlaceholderApi处理器处理主键 '{}' 时错误: {}", mainKey, e);
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        ModData.LOGGER.error("PlaceholderApi处理器处理主键 '{}' 时错误: {}", mainKey, cause);
                    }
                }
            }

            if (replacement != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

}