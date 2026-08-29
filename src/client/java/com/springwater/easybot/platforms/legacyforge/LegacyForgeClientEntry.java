//? legacyforge {
package com.springwater.easybot.platforms.legacyforge;

import com.springwater.easybot.clientip.ClientIpPacket;
import com.springwater.easybot.clientip.ClientIpResolver;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.platforms.legacyforge.features.ClientIpSyncFeature;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ModData.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LegacyForgeClientEntry {
    private static final AtomicBoolean requested = new AtomicBoolean(false);
    private static final AtomicBoolean sent = new AtomicBoolean(false);

    private LegacyForgeClientEntry() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(LegacyForgeClientEntry::onClientTick);
    }

    private static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || sent.get()) {
            return;
        }

        final Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) {
            return;
        }

        try {
            if (ConfigLoader.get().getClient() == null || !ConfigLoader.get().getClient().isEnableClientIpSync()) {
                sent.set(true);
                return;
            }
        } catch (IllegalStateException ignored) {
            sent.set(true);
            return;
        }

        if (!requested.compareAndSet(false, true)) {
            return;
        }

        CompletableFuture.supplyAsync(ClientIpResolver::fetchPublicIp)
                .thenAccept(ip -> client.execute(() -> sendIp(client, ip)));
    }

    private static void sendIp(final Minecraft client, final String ip) {
        if (sent.get()) {
            return;
        }

        if (ip == null || ip.isBlank() || client.player == null) {
            sent.set(true);
            return;
        }

        try {
            ClientIpSyncFeature.sendToServer(new ClientIpPacket(
                    client.player.getName().getString(),
                    client.player.getUUID().toString(),
                    ip
            ));
        } catch (Exception ignored) {
            sent.set(true);
            return;
        }

        sent.set(true);
    }
}
//?}
