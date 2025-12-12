package com.springwater.easybot.placeholder.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 统计数据解析工具类。
 * 用于从特定格式的字符串（例如：StatisticName$i=item$e=entity）中提取信息。
 */
public final class StatisticsParser {
    private static final Pattern ITEM_PATTERN = Pattern.compile("\\$i=([^$&]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENTITY_PATTERN = Pattern.compile("\\$e=([^$&]+)", Pattern.CASE_INSENSITIVE);

    // 私有构造函数，防止实例化
    private StatisticsParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 获取统计类型名称。
     * <p>
     * 优化说明：移除了复杂的正则匹配。
     * 逻辑：截取第一个 '$' 符号之前的内容。如果不存在 '$'，则返回原始内容。
     * 这种方式比 Regex 快得多，且能处理绝大多数情况。
     *
     * @param content 输入内容
     * @return 统计类型名称
     */
    public static String getStatistic(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        int index = content.indexOf('$');
        // 如果没有找到分隔符，直接返回原始内容；否则截取分隔符前的部分
        return index == -1 ? content : content.substring(0, index);
    }

    /**
     * 获取物品名称。
     *
     * @param content 输入内容
     * @return 物品名称，如果未找到或输入为空则返回 null
     */
    public static String getItem(String content) {
        return parseByPattern(ITEM_PATTERN, content);
    }

    /**
     * 获取实体名称。
     *
     * @param content 输入内容
     * @return 实体名称，如果未找到或输入为空则返回 null
     */
    public static String getEntity(String content) {
        return parseByPattern(ENTITY_PATTERN, content);
    }

    /**
     * 内部通用解析方法，减少代码重复。
     *
     * @param pattern 匹配模式
     * @param content 输入内容
     * @return 捕获组内容
     */
    private static String parseByPattern(Pattern pattern, String content) {
        if (content == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}