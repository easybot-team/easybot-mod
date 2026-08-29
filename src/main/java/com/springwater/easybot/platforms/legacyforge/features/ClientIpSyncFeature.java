//? legacyforge {
package com.springwater.easybot.platforms.legacyforge.features;

import com.springwater.easybot.clientip.ClientIpBridgeForwarder;
import com.springwater.easybot.clientip.ClientIpPacket;
import com.springwater.easybot.features.IEasyBotFeatures;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ClientIpSyncFeature implements IEasyBotFeatures {
    private static final String PROTOCOL = "1";
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ModData.MOD_ID, "client_ip_report"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    @Override
    public void register() {
        ensureRegistered();
    }

    public static void ensureRegistered() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }

        CHANNEL.messageBuilder(ClientIpPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ClientIpPacket::encode)
                .decoder(ClientIpPacket::decode)
                .consumerMainThread(ClientIpSyncFeature::handleServer)
                .add();
    }

    public static void sendToServer(ClientIpPacket packet) {
        ensureRegistered();
        CHANNEL.sendToServer(packet);
    }

    private static void handleServer(final ClientIpPacket packet, final Supplier<NetworkEvent.Context> contextSupplier) {
        final NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientIpBridgeForwarder.forward(packet.name(), packet.uuid(), packet.ip()));
        context.setPacketHandled(true);
    }
}
//?}
