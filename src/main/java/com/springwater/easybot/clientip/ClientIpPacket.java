package com.springwater.easybot.clientip;

import net.minecraft.network.FriendlyByteBuf;
//? fabric {
//? >=1.20.6 {
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}
//?}
//? neoforge {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? <=1.20.4 {
import net.minecraft.resources.ResourceLocation;
//?}
//? >1.20.4 {
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
//?}
//?}

public record ClientIpPacket(String name, String uuid, String ip)
//? fabric {
//? >=1.20.6 {
        implements CustomPacketPayload
//?}
//?}
//? neoforge {
        implements CustomPacketPayload
//?}
{
    public static final int MAX_IP_LENGTH = 128;
    public static final int MAX_NAME_LENGTH = 64;
    public static final int MAX_UUID_LENGTH = 64;

    //? fabric {
    //? >=1.20.6 {
    public static final CustomPacketPayload.Type<ClientIpPacket> TYPE = new CustomPacketPayload.Type<>(
            //? <1.21 {
            new net.minecraft.resources.ResourceLocation("easybot", "client_ip_report")
            //?}
            //? >=1.21 {
            //? >=1.21.11 {
            net.minecraft.resources.Identifier.fromNamespaceAndPath("easybot", "client_ip_report")
            //?}
            //? <1.21.11 {
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("easybot", "client_ip_report")
            //?}
            //?}
    );
    public static final StreamCodec<ByteBuf, ClientIpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ClientIpPacket::name,
            ByteBufCodecs.STRING_UTF8,
            ClientIpPacket::uuid,
            ByteBufCodecs.STRING_UTF8,
            ClientIpPacket::ip,
            ClientIpPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
    //?}

    //? neoforge {
    //? <=1.20.4 {
    public static final ResourceLocation ID = new ResourceLocation("easybot", "client_ip_report");

    public ClientIpPacket(final FriendlyByteBuf buffer) {
        this(buffer.readUtf(MAX_NAME_LENGTH), buffer.readUtf(MAX_UUID_LENGTH), buffer.readUtf(MAX_IP_LENGTH));
    }

    public void write(final FriendlyByteBuf buffer) {
        buffer.writeUtf(name(), MAX_NAME_LENGTH);
        buffer.writeUtf(uuid(), MAX_UUID_LENGTH);
        buffer.writeUtf(ip(), MAX_IP_LENGTH);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
    //?}

    //? >1.20.4 {
    public static final CustomPacketPayload.Type<ClientIpPacket> TYPE = new CustomPacketPayload.Type<>(
            //? >=1.21 {
            ResourceLocation.fromNamespaceAndPath("easybot", "client_ip_report")
            //?}
            //? <1.21 {
            new ResourceLocation("easybot", "client_ip_report")
            //?}
    );
    public static final StreamCodec<ByteBuf, ClientIpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ClientIpPacket::name,
            ByteBufCodecs.STRING_UTF8,
            ClientIpPacket::uuid,
            ByteBufCodecs.STRING_UTF8,
            ClientIpPacket::ip,
            ClientIpPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
    //?}

    public static void encode(ClientIpPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.name(), MAX_NAME_LENGTH);
        buffer.writeUtf(packet.uuid(), MAX_UUID_LENGTH);
        buffer.writeUtf(packet.ip(), MAX_IP_LENGTH);
    }

    public static ClientIpPacket decode(FriendlyByteBuf buffer) {
        return new ClientIpPacket(
                buffer.readUtf(MAX_NAME_LENGTH),
                buffer.readUtf(MAX_UUID_LENGTH),
                buffer.readUtf(MAX_IP_LENGTH)
        );
    }
}
