package com.springwater.easybot.placeholder.handlers;
//? >= 1.21.11 {

import com.google.gson.Gson;
//?}
import com.springwater.easybot.ModFlags;
import com.springwater.easybot.api.IPlaceholderHandler;
import com.springwater.easybot.mixin.ServerStatsCounterAccessor;
import com.springwater.easybot.placeholder.utils.StatisticsParser;
import com.springwater.easybot.statistic.StatisticManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class StatisticHandler implements IPlaceholderHandler {
    @Override
    public String getPrefix() {
        return "ez-statistic";
    }

    //? >= 1.21.11 {
    private static final Gson GSON = new Gson();

    //?}
    private static String realTimeStatToJson(ServerPlayer player) {
        //? >= 1.21.11 {
        return GSON.toJson(((ServerStatsCounterAccessor) (player.getStats())).easybot$toJson());
        //?} else {
        /*return ((ServerStatsCounterAccessor) (player.getStats())).easybot$toJson();
         *///?}
    }

    public @Nullable String handle(String argumentWithoutPrefix, String playerName, @Nullable ServerPlayer player) {
        if (!ModFlags.isPlayerStatisticSupported())
            return null;
        String statisticKey = StatisticsParser.getStatistic(argumentWithoutPrefix);
        if (statisticKey == null) return null;
        var stat = player == null
                ? StatisticManager.getInstance().getPlayerStat(playerName) :
                StatisticManager.getInstance().loadPlayerStat(realTimeStatToJson(player));
        var item = StatisticsParser.getItem(argumentWithoutPrefix);
        var entity = StatisticsParser.getEntity(argumentWithoutPrefix);
        return switch (statisticKey.toLowerCase()) {
            case "drop" -> {
                if (item == null) yield null;
                yield stat.getDropped(item).orElse("0");
            }
            case "pickup" -> {
                if (item == null) yield null;
                yield stat.getPickedUp(item).orElse("0");
            }
            case "mine_block" -> {
                if (item == null) yield null;
                yield stat.getMined(item).orElse("0");
            }
            case "use_item" -> {
                if (item == null) yield null;
                yield stat.getUsed(item).orElse("0");
            }
            case "break_item" -> {
                if (item == null) yield null;
                yield stat.getBroken(item).orElse("0");
            }
            case "craft_item" -> {
                if (item == null) yield null;
                yield stat.getCrafted(item).orElse("0");
            }
            case "kill_entity" -> {
                if (entity == null) yield null;
                yield stat.getEntityKilled(entity).orElse("0");
            }
            case "entity_killed_by" -> {
                if (entity == null) yield null;
                yield stat.getEntityKilledBy(entity).orElse("0");
            }
            default -> stat.getCustom(statisticKey).orElse("0");
        };
    }


    @Override
    public @Nullable String replacePlaceholders(String argumentWithoutPrefix, String playerName, @Nullable ServerPlayer player) {
        return handle(argumentWithoutPrefix, playerName, player);
    }

    @Override
    public @Nullable String replacePlaceholders(String argumentWithoutPrefix, String playerName, MinecraftServer server) {
        return handle(argumentWithoutPrefix, playerName, null);
    }
}
