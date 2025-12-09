package com.springwater.easybot.placeholder;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.utils.TextUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class PlaceholderApi {

    private static String replaceInternal(String text, String playerName) {
        var pattern = Pattern.compile("%([^%]+)%");
        var matcher = pattern.matcher(text);
        while (matcher.find()) {
            String placeholder = "%" + matcher.group(1) + "%";
            var value = PlaceholderApiMappings.PLACEHOLDER_API_MAPPINGS.getOrDefault(placeholder, placeholder);
            text = text.replace(placeholder, value);
        }
        text = text.replace("%player:name%", playerName);
        return text;
    }

    public static String replacePlaceholders(String text, String playerName, @Nullable ServerPlayer player) {
        text = replaceInternal(text, playerName);
        if (!ClientProfile.isPapiSupported()) {
            EasyBotFabric.LOGGER.warn("不支持Papi查询,即将“原路返回”");
            return text;
        }
        var context = player == null
                ? PlaceholderContext.of(EasyBotFabric.getServer())
                : PlaceholderContext.of(player);
        var placeholders = Component.literal(text);
        var result = Placeholders.parseText(placeholders, context);
        return TextUtils.toLegacyString(result);
    }
}
