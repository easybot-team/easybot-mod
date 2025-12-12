package com.springwater.easybot.placeholder.handlers;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.api.IPlaceholderHandler;
import com.springwater.easybot.placeholder.PlaceholderManager;
import com.springwater.easybot.placeholder.TextPlaceholderApiMod;
import com.springwater.easybot.utils.TextUtils;
import lombok.SneakyThrows;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathHandler implements IPlaceholderHandler {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

    @Override
    public String getPrefix() {
        return "math";
    }

    private String doMath(String text, String playerName, @Nullable ServerPlayer player) throws EvaluationException, ParseException {
        Integer scale = 2;
        RoundingMode roundingMode = RoundingMode.CEILING;
        String expressionString = text;
        int underscoreIndex = text.indexOf('_');
        if (underscoreIndex > 0) {
            String configPart = text.substring(0, underscoreIndex);
            if (configPart.startsWith("_")) {
                configPart = configPart.substring(1);
            }

            if (configPart.contains(":")) {
                String[] config = configPart.split(":");
                if (config.length == 2) {
                    try {
                        int parsedScale = Integer.parseInt(config[0]);
                        RoundingMode parsedMode = parseRoundingMode(config[1]);
                        scale = parsedScale;
                        roundingMode = parsedMode;
                        expressionString = text.substring(underscoreIndex + 1);
                    } catch (Exception e) {
                        EasyBotFabric.LOGGER.error("解析配置失败: {} {}", text, e);
                    }
                }
            }
        }
        String intermediate = TextUtils.clearStyleCode(replaceDynamic(expressionString, playerName, player));
        var expression = new Expression(intermediate.trim());
        var result = expression.evaluate();

        // 4. 格式化输出
        if (scale != null && roundingMode != null) {
            BigDecimal decimalResult = result.getNumberValue();
            decimalResult = decimalResult.setScale(scale, roundingMode);
            return decimalResult.toPlainString();
        }

        return result.getStringValue();
    }

    private RoundingMode parseRoundingMode(String input) {
        // 尝试解析为数字 (兼容Bukkit PAPI 的 Math)
        // 0=UP, 1=DOWN, 2=CEILING, 3=FLOOR, 4=HALF_UP, 5=HALF_DOWN, 6=HALF_EVEN, 7=UNNECESSARY
        try {
            int modeInt = Integer.parseInt(input);
            switch (modeInt) {
                case 0:
                    return RoundingMode.UP;
                case 1:
                    return RoundingMode.DOWN;
                case 2:
                    return RoundingMode.CEILING; // 向正无穷舍入
                case 3:
                    return RoundingMode.FLOOR;   // 向负无穷舍入
                case 4:
                    return RoundingMode.HALF_UP; // 四舍五入
                case 5:
                    return RoundingMode.HALF_DOWN;
                case 6:
                    return RoundingMode.HALF_EVEN;
                case 7:
                    return RoundingMode.UNNECESSARY;
                default:
                    break; // 继续尝试 Enum 解析
            }
        } catch (NumberFormatException ignored) {
        }

        // 尝试解析为枚举名称 (不区分大小写)
        return RoundingMode.valueOf(input.toUpperCase());
    }

    private String replaceDynamic(String text, String playerName, @Nullable ServerPlayer player) {
        String intermediate = TextPlaceholderApiMod.replacePlaceholders(text, playerName, player);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(intermediate);
        if (!matcher.find()) return intermediate;

        matcher.reset();
        StringBuilder sb = new StringBuilder(intermediate.length() + 32);
        while (matcher.find()) {
            String query = "%" + matcher.group(1) + "%";
            String placement = PlaceholderManager.getInstance().replacePlaceholders(query, playerName, player);
            if (placement != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(placement));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @SneakyThrows
    @Override
    public @Nullable String replacePlaceholders(String argumentWithoutPrefix, String playerName, @Nullable ServerPlayer player) {
        return doMath(argumentWithoutPrefix, playerName, player);
    }

    @SneakyThrows
    @Override
    public @Nullable String replacePlaceholders(String argumentWithoutPrefix, String playerName, MinecraftServer server) {
        return doMath(argumentWithoutPrefix, playerName, null);
    }
}