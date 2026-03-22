package com.springwater.easybot.utils;

// 饿啊啊啊啊
// 麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你
// 麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你
// 麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你麻将我恨你

import com.mojang.authlib.GameProfile;

public class GameProfileUtils {
    public static String getName(GameProfile profile) {
        //? if >=1.21.9 {
        return profile.name();
        //?} else {
        /*return profile.getName();
         *///?}
    }

    public static String getUuid(GameProfile profile) {
        //? if >=1.21.9 {
        return profile.id().toString();
        //?} else {
        /*return profile.getId().toString();
         *///?}
    }
}
