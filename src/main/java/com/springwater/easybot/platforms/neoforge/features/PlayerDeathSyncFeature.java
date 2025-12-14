//? neoforge {
package com.springwater.easybot.platforms.neoforge.features;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.EasyBotModImpl;
import com.springwater.easybot.threading.EasyBotNetworkingThreadPool;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerDeathSyncFeature implements IEasyBotFeatures {
    public static final Map<String, String> DEATH_MESSAGES = new HashMap<>();

    public PlayerDeathSyncFeature() {
        // 这部分死亡信息是通用的，无需修改
        DEATH_MESSAGES.put("arrow", "%s 被箭射杀");
        DEATH_MESSAGES.put("trident", "%s 被三叉戟刺死了");
        DEATH_MESSAGES.put("mob_projectile", "%s 被射杀");
        DEATH_MESSAGES.put("spit", "%s 被羊驼啐了一脸唾沫");
        DEATH_MESSAGES.put("thrown", "%s 被砸死了");
        DEATH_MESSAGES.put("wither_skull", "%s 被凋灵首级射杀");
        DEATH_MESSAGES.put("explosion", "%s 爆炸了");
        DEATH_MESSAGES.put("player_explosion", "%s 被玩家炸死了");
        DEATH_MESSAGES.put("fireworks", "%s 在一声巨响中消失了");
        DEATH_MESSAGES.put("bad_respawn_point", "%s 被 [即兴游戏设计] 杀死了");
        DEATH_MESSAGES.put("wind_charge", "%s 被风弹击杀");
        DEATH_MESSAGES.put("on_fire", "%s 被烧死了");
        DEATH_MESSAGES.put("in_fire", "%s 浴火焚身");
        DEATH_MESSAGES.put("lava", "%s 试图在熔岩里游泳");
        DEATH_MESSAGES.put("hot_floor", "%s 发现地面是熔岩做的");
        DEATH_MESSAGES.put("campfire", "%s 走进了营火");
        DEATH_MESSAGES.put("fireball", "%s 被火球炸死了");
        DEATH_MESSAGES.put("unattributed_fireball", "%s 被火球炸死了");
        DEATH_MESSAGES.put("dragon_breath", "%s 被龙息烤熟了");
        DEATH_MESSAGES.put("fall", "%s 落地过猛");
        DEATH_MESSAGES.put("fly_into_wall", "%s 感受到了动能");
        DEATH_MESSAGES.put("in_wall", "%s 在墙里窒息了");
        DEATH_MESSAGES.put("cramming", "%s 被挤扁了");
        DEATH_MESSAGES.put("drown", "%s 淹死了");
        DEATH_MESSAGES.put("starve", "%s 饿死了");
        DEATH_MESSAGES.put("cactus", "%s 被刺死了");
        DEATH_MESSAGES.put("sweet_berry_bush", "%s 被甜浆果丛刺死了");
        DEATH_MESSAGES.put("freeze", "%s 冻死了");
        DEATH_MESSAGES.put("stalagmite", "%s 被石笋刺穿了");
        DEATH_MESSAGES.put("lightning_bolt", "%s 被闪电劈死了");
        DEATH_MESSAGES.put("falling_block", "%s 被下落的方块压扁了");
        DEATH_MESSAGES.put("falling_anvil", "%s 被下落的铁砧压扁了");
        DEATH_MESSAGES.put("falling_stalactite", "%s 被下落的钟乳石刺穿了");
        DEATH_MESSAGES.put("magic", "%s 被魔法杀死了");
        DEATH_MESSAGES.put("indirect_magic", "%s 被魔法杀死了");
        DEATH_MESSAGES.put("wither", "%s 凋零了");
        DEATH_MESSAGES.put("thorns", "%s 在试图伤害敌人时被杀");
        DEATH_MESSAGES.put("sonic_boom", "%s 被一声尖啸不仅震碎了耳膜，还震碎了身躯");
        DEATH_MESSAGES.put("mob_attack", "%s 被怪物杀死了");
        DEATH_MESSAGES.put("mob_attack_no_aggro", "%s 被猛撞而死");
        DEATH_MESSAGES.put("player_attack", "%s 被玩家杀死了");
        DEATH_MESSAGES.put("sting", "%s 被蛰死了");
        DEATH_MESSAGES.put("mace_smash", "%s 被重锤砸扁了");
        DEATH_MESSAGES.put("out_of_world", "%s 掉出了这个世界");
        DEATH_MESSAGES.put("outside_border", "%s 离开了这个世界的边缘");
        DEATH_MESSAGES.put("generic", "%s 死了");
        DEATH_MESSAGES.put("generic_kill", "%s 死了");
        DEATH_MESSAGES.put("dry_out", "%s 脱水了");
        DEATH_MESSAGES.put("ender_pearl", "%s 因投掷末影珍珠而死");
    }

    @Override
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (ConfigLoader.get().getSkipOptions().isSkipDeath()) return;

        // 从事件中获取死亡的实体
        var entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            // 从事件中获取伤害来源
            var source = event.getSource();

            var profile = PlayerUtils.getPlayerInfo(player);
            var killer = new StringBuilder();
            var deathReason = new StringBuilder();

            // 核心逻辑与 Fabric 版本几乎完全相同
            if (source.getEntity() instanceof LivingEntity livingEntity) {
                killer.append(getKillerName(livingEntity));
                deathReason.append(profile.name).append(" 被 ").append(killer).append(" 杀死了");
            } else {
                var key = source.typeHolder().unwrapKey();
                if (key.isPresent()) {
                    var path = key.get().location().getPath();
                    if (player.getKillCredit() != null) {
                        killer.append(getKillerName(player.getKillCredit()));
                    } else {
                        killer.append("一股神秘的力量");
                    }
                    deathReason.append(DEATH_MESSAGES.getOrDefault(path, "%s 死了").replace("%s", profile.name));
                }
            }
            EasyBotNetworkingThreadPool.getInstance().addTask(() -> EasyBotModImpl.INSTANCE.getBridgeClient().syncDeathMessage(profile, deathReason.toString(), killer.toString()), "消息同步-死亡");
        }
    }

    private String getKillerName(LivingEntity entity) {
        // 这个方法是通用的，无需修改
        if (entity.getCustomName() != null) return entity.getCustomName().getString();
        return entity.getName().getString();
    }
}

//?}