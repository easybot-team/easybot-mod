//? neoforge {
package com.springwater.easybot.platforms.neoforge;

import com.springwater.easybot.clientip.ClientIpPacket;
import com.springwater.easybot.clientip.ClientIpResolver;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.ClientPacketDistributor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ModData.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NeoForgeClientEntry {
    private static final AtomicBoolean requested = new AtomicBoolean(false);
    private static final AtomicBoolean sent = new AtomicBoolean(false);

    private NeoForgeClientEntry() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(NeoForgeClientEntry::onClientTick);
    }

    private static void onClientTick(final ClientTickEvent.Post event) {
        if (sent.get()) {
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
            ClientPacketDistributor.sendToServer(new ClientIpPacket(
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
