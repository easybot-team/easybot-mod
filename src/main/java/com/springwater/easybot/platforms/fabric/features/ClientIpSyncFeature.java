//? fabric {
package com.springwater.easybot.platforms.fabric.features;

import com.springwater.easybot.clientip.ClientIpBridgeForwarder;
import com.springwater.easybot.clientip.ClientIpPacket;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.ModData;
//? >=1.20.6 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?}
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? <1.20.6 {
import net.minecraft.resources.ResourceLocation;
//?}

public class ClientIpSyncFeature implements IEasyBotFeatures {
    //? <1.20.6 {
    public static final ResourceLocation CLIENT_IP_REPORT_ID = new ResourceLocation(ModData.MOD_ID, "client_ip_report");
    //?}

    @Override
    public void register() {
        //? >=1.20.6 {
        registerPayloadType();
        registerReceiver();
        //?}
        //? <1.20.6 {
        registerLegacyReceiver();
        //?}
    }

    //? >=1.20.6 {
    public static void registerPayloadType() {
        PayloadTypeRegistry.playC2S().register(ClientIpPacket.TYPE, ClientIpPacket.STREAM_CODEC);
    }

    private static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ClientIpPacket.TYPE, (payload, context) ->
                context.server().execute(() -> ClientIpBridgeForwarder.forward(payload.name(), payload.uuid(), payload.ip())));
    }
    //?}

    //? <1.20.6 {
    public static void registerPayloadType() {
    }

    private static void registerLegacyReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CLIENT_IP_REPORT_ID, (server, player, handler, buf, responseSender) -> {
            var packet = ClientIpPacket.decode(buf);
            server.execute(() -> handleReport(packet));
        });
    }
    //?}

    private static void handleReport(ClientIpPacket packet) {
        String ip = packet.ip();
        if (ConfigLoader.get().isDebug()) {
            ModData.LOGGER.info("收到客户端IP上报 {} ({}) -> {}", packet.name(), packet.uuid(), ip);
        }
        ClientIpBridgeForwarder.forward(packet.name(), packet.uuid(), ip);
    }
}
//?}
