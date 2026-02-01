//? legacyforge {
package com.springwater.easybot.platforms.legacyforge.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.legacyforge.LegacyForgeEntry;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.springwater.easybot.utils.DamageTypeMappings.DEATH_MESSAGES;

public class PlayerDeathSyncFeature implements IEasyBotFeatures {

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (ConfigLoader.get().getSkipOptions().isSkipDeath()) return;
        if (LegacyForgeEntry.getBridgeClient() == null || !LegacyForgeEntry.getBridgeClient().isReady()) {
            return;
        }

        var entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            var source = event.getSource();

            var profile = PlayerUtils.getPlayerInfo(player);
            var killer = new StringBuilder();
            var deathReason = new StringBuilder();

            if (source.getEntity() instanceof LivingEntity livingEntity) {
                killer.append(getKillerName(livingEntity));
                deathReason.append(profile.name).append(" 被 ").append(killer).append(" 杀死了");
            } else {
                String path;

                // 适配不同版本的 DamageSource 获取 ID 方式
                //? if >=1.19.4 {
                path = source.type().msgId();
                //?} else {
                /*path = source.getMsgId();
                 *///?}

                //noinspection ConstantValue
                if (path != null) {
                    if (player.getKillCredit() != null) {
                        killer.append(getKillerName(player.getKillCredit()));
                    } else {
                        killer.append("一股神秘的力量");
                    }
                    deathReason.append(DEATH_MESSAGES.getOrDefault(path, "%s 死了").replace("%s", profile.name));
                }
            }

            EasyBotNetworkingThreadPool.getInstance().addTask(() ->
                    LegacyForgeEntry.getBridgeClient().syncDeathMessage(profile, deathReason.toString(), killer.toString()), "消息同步-死亡");
        }
    }

    private String getKillerName(LivingEntity entity) {
        if (entity.getCustomName() != null) return entity.getCustomName().getString();
        return entity.getName().getString();
    }
}
//?}
