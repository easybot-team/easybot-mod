package com.springwater.easybot.placeholder;

import java.util.HashMap;
import java.util.Map;

/**
 * Bukkit版PlaceholderApi与 TextPlaceholderApi的部分映射关系
 */
public class PlaceholderApiMappings {
    public static final Map<String, String> PLACEHOLDER_API_MAPPINGS = new HashMap<>();

    static {
        /*
         * ==========================================
         * Mappings: Player
         * ECloud: https://api.extendedclip.com/expansions/player/
         *
         * 不支持的占位符 (共118个) 不加入Map，包括但不限于:
         * - 所有装备属性 (%player_armor_helmet_name% 等)
         * - 床位置相关 (%player_bed_x% 等)
         * - 玩家状态 (%player_is_flying%, %player_is_sneaking% 等)
         * - NBT/物品数据 (%player_item_in_hand_data% 等)
         * - 世界/时间细节 (%player_world_time_12%, %player_thunder_duration% 等)
         * - 旧版移除特性 (%player_health_scale%, %player_absorption% 等)
         * - 部分信息 (%player_ip% 等)
         *
         * ==========================================
         */

        PLACEHOLDER_API_MAPPINGS.put("%player_name%", "%player:name%");
        PLACEHOLDER_API_MAPPINGS.put("%player_displayname%", "%player:displayname%");
        PLACEHOLDER_API_MAPPINGS.put("%player_uuid%", "%player:uuid%");
        PLACEHOLDER_API_MAPPINGS.put("%player_ping%", "%player:ping%");
        PLACEHOLDER_API_MAPPINGS.put("%player_x%", "%player:pos_x%");
        PLACEHOLDER_API_MAPPINGS.put("%player_y%", "%player:pos_y%");
        PLACEHOLDER_API_MAPPINGS.put("%player_z%", "%player:pos_z%");
        PLACEHOLDER_API_MAPPINGS.put("%player_health%", "%player:health%");
        PLACEHOLDER_API_MAPPINGS.put("%player_max_health%", "%player:max_health%");
        PLACEHOLDER_API_MAPPINGS.put("%player_food_level%", "%player:hunger%");
        PLACEHOLDER_API_MAPPINGS.put("%player_saturation%", "%player:saturation%");
        PLACEHOLDER_API_MAPPINGS.put("%player_biome%", "%player:biome%"); // 需2.7.2+
        PLACEHOLDER_API_MAPPINGS.put("%player_direction_xz%", "%player:facing%"); // 需2.5.1+
        PLACEHOLDER_API_MAPPINGS.put("%player_play_time%", "%player:playtime%"); // 格式化时间
        PLACEHOLDER_API_MAPPINGS.put("%player_head%", "%player:head%"); // 需2.8.0+


        /*
         * ==========================================
         * Mappings: Server
         * ECloud: https://api.extendedclip.com/expansions/server/
         *
         * 不支持的占位符（不加入Map），包括例如:
         * - %server_version_full%（新版无对应）
         * - %server_variant%（新版无对应）
         * - %server_build%（新版无对应）
         * - %server_online_(world)%（新版无“按世界人数”）
         * - %server_ram_free%, %server_ram_total%（新版仅 used/max）
         * - %server_unique_joins%
         * - 所有 countup / countdown 系列（新版无）
         * - TPS 历史窗口 (%server_tps_1%, 5, 15) 新版只有当前TPS
         * - Whitelist、Chunk、Entity 统计（新版无）
         *
         * ==========================================
         */

        // 基础信息
        PLACEHOLDER_API_MAPPINGS.put("%server_name%", "%server:name%");
        PLACEHOLDER_API_MAPPINGS.put("%server_version%", "%server:version%");
        PLACEHOLDER_API_MAPPINGS.put("%server_motd%", "%server:motd%");
        PLACEHOLDER_API_MAPPINGS.put("%server_online%", "%server:online%");
        PLACEHOLDER_API_MAPPINGS.put("%server_max_players%", "%server:max_players%");

        // TPS（新版没有 1分/5分/15分窗口）
        PLACEHOLDER_API_MAPPINGS.put("%server_tps%", "%server:tps%");
        PLACEHOLDER_API_MAPPINGS.put("%server_tps_1%", "%server:tps%");        // 降级映射
        PLACEHOLDER_API_MAPPINGS.put("%server_tps_5%", "%server:tps%");        // 降级映射
        PLACEHOLDER_API_MAPPINGS.put("%server_tps_15%", "%server:tps%");       // 降级映射
        PLACEHOLDER_API_MAPPINGS.put("%server_tps_1_colored%", "%server:tps_colored%");
        PLACEHOLDER_API_MAPPINGS.put("%server_tps_5_colored%", "%server:tps_colored%");
        PLACEHOLDER_API_MAPPINGS.put("%server_tps_15_colored%", "%server:tps_colored%");

        // RAM
        PLACEHOLDER_API_MAPPINGS.put("%server_ram_used%", "%server:used_ram%");
        PLACEHOLDER_API_MAPPINGS.put("%server_ram_max%", "%server:max_ram%");

        // 时间
        PLACEHOLDER_API_MAPPINGS.put("%server_uptime%", "%server:uptime%");
    }
}