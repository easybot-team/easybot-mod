package com.springwater.easybot.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLoginPacketListenerImpl.class)
public interface ServerLoginNetworkHandlerAccessor {
    //? if >1.20.1 {
    @Accessor("authenticatedProfile")
    //?} else {
    /*@Accessor("gameProfile")
     *///?}
    GameProfile getGameProfile();
    @Accessor("connection")
    Connection GetConnection();
}