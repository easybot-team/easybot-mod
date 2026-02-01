//? neoforge {
/*package com.springwater.easybot.platforms.neoforge.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.CarpetUtils;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import static com.springwater.easybot.utils.DamageTypeMappings.DEATH_MESSAGES;

public class PlayerDeathSyncFeature implements IEasyBotFeatures {

    @Override
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (ConfigLoader.get().getSkipOptions().isSkipDeath()) return;

        var entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            if (CarpetUtils.isFakePlayer((ServerPlayer)entity)) {
                if (ConfigLoader.get().isDebug()) {
                    ModData.LOGGER.info("已过滤地毯假人 {}", entity.getName().getString());
                }
                return;
            }
            
            var source = event.getSource();

            var profile = PlayerUtils.getPlayerInfo(player);
            var killer = new StringBuilder();
            var deathReason = new StringBuilder();

            var key = source.typeHolder().unwrapKey();
            //? >=1.21.11 {
            /^if (key.isPresent() && !key.get().identifier().getPath().equals("mob_attack")) {
            ^///?} else {
            if (key.isPresent() && !key.get().location().getPath().equals("mob_attack")) {
             //?}
                //? >=1.21.11 {
                /^var path = key.get().identifier().getPath();
                ^///?} else {
                var path = key.get().location().getPath();
                 //?}
                if (player.getKillCredit() != null) {
                    killer.append(getKillerName(player.getKillCredit()));
                } else {
                    killer.append("一股神秘的力量");
                }
                deathReason.append(DEATH_MESSAGES.getOrDefault(path, "%s 死了").replace("%s", profile.name));
            }else if (source.getEntity() instanceof LivingEntity livingEntity) {
                killer.append(getKillerName(livingEntity));
                deathReason.append(profile.name).append(" 被 ").append(killer).append(" 杀死了");
            }else{
                deathReason.append(profile.name).append(" 死了");
            }
            EasyBotNetworkingThreadPool.getInstance().addTask(() -> EasyBotModImpl.INSTANCE.getBridgeClient().syncDeathMessage(profile, deathReason.toString(), killer.toString()), "消息同步-死亡");
        }
    }

    private String getKillerName(LivingEntity entity) {
        if (entity.getCustomName() != null) return entity.getCustomName().getString();
        return entity.getName().getString();
    }
}

*///?}