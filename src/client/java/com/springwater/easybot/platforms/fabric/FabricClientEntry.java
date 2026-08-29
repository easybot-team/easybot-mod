//? fabric {
package com.springwater.easybot.platforms.fabric;

import com.springwater.easybot.clientip.ClientIpPacket;
import com.springwater.easybot.clientip.ClientIpResolver;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.fabric.features.ClientIpSyncFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class FabricClientEntry implements ClientModInitializer {
    private static final AtomicBoolean requested = new AtomicBoolean(false);
    private static final AtomicBoolean sent = new AtomicBoolean(false);

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        //? >=1.20.6 {
        ClientIpSyncFeature.registerPayloadType();
        //?}
    }

    private void onClientTick(MinecraftClient client) {
        if (sent.get()) {
            return;
        }

        if (client == null || client.player == null || client.getNetworkHandler() == null) {
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

        CompletableFuture.supplyAsync(ClientIpResolver::fetchPublicIp).thenAccept(ip -> client.execute(() -> sendIp(client, ip)));
    }

    private void sendIp(MinecraftClient client, String ip) {
        if (sent.get()) {
            return;
        }

        if (ip == null || ip.isBlank()) {
            sent.set(true);
            return;
        }

        //? >=1.20.6 {
        try {
            if (!ClientPlayNetworking.canSend(ClientIpPacket.TYPE)) {
                sent.set(true);
                return;
            }
            String name = client.player.getName().getString();
            String uuid = client.player.getUUID().toString();
            ClientPlayNetworking.send(new ClientIpPacket(name, uuid, ip));
        } catch (Exception e) {
            sent.set(true);
            return;
        }
        //?}
        //? <1.20.6 {
        try {
            var buffer = PacketByteBufs.create();
            String name = client.player.getName().getString();
            String uuid = client.player.getUUID().toString();
            ClientIpPacket.encode(new ClientIpPacket(name, uuid, ip), buffer);
            ClientPlayNetworking.send(ClientIpSyncFeature.CLIENT_IP_REPORT_ID, buffer);
        } catch (Exception e) {
            sent.set(true);
            return;
        }
        //?}
        sent.set(true);
    }
}
//?}
