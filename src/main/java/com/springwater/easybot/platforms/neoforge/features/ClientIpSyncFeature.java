//? neoforge {
package com.springwater.easybot.platforms.neoforge.features;

import com.springwater.easybot.clientip.ClientIpBridgeForwarder;
import com.springwater.easybot.clientip.ClientIpPacket;
import com.springwater.easybot.features.IEasyBotFeatures;
//? <=1.20.4 {
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
//?}
//? >1.20.4 {
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
//?}

public final class ClientIpSyncFeature {
    private ClientIpSyncFeature() {
    }

    //? <=1.20.4 {
    public static void registerPayloads(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar("easybot");
        registrar.play(ClientIpPacket.ID, ClientIpPacket::new, handler -> handler.server(ClientIpSyncFeature::handleServer));
    }

    private static void handleServer(final ClientIpPacket packet, final IPayloadContext context) {
        context.workHandler().submitAsync(() -> ClientIpBridgeForwarder.forward(packet.name(), packet.uuid(), packet.ip()));
    }
    //?}

    //? >1.20.4 {
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ClientIpPacket.TYPE, ClientIpPacket.STREAM_CODEC, ClientIpSyncFeature::handleServer);
    }

    private static void handleServer(final ClientIpPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientIpBridgeForwarder.forward(packet.name(), packet.uuid(), packet.ip()));
    }
    //?}
}
//?}
