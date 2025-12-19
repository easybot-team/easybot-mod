package com.springwater.easybot.utils;

import net.minecraft.server.level.ServerPlayer;

public class CarpetUtils {
    public static boolean isFakePlayer(ServerPlayer  player){
        return player.getClass().getName().contains("EntityPlayerMPFake");
    }
}
