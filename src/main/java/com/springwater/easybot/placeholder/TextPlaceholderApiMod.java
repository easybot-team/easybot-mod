//? fabric {
package com.springwater.easybot.placeholder;
import com.springwater.easybot.ModFlags;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.utils.TextUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextPlaceholderApiMod {
    public static String replacePlaceholders(String text, @Nullable ServerPlayer player) {
        if (!ModFlags.isTextPlaceholderApiInstalled()) {
            return text;
        }
        try {
            var context = (player == null)
                    ? PlaceholderContext.of(EasyBotModImpl.INSTANCE.getServer())
                    : PlaceholderContext.of(player);
            var placeholders = Component.literal(text);
            var result = Placeholders.parseText(placeholders, context);
            return TextUtils.toLegacyString(result);
        } catch (Exception e) {
            ModData.LOGGER.error("TextPlaceholderApi 调用失败：{}", e.getMessage());
            return text;
        }
    }
}
//?}