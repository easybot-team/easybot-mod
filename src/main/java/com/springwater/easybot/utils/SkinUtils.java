package com.springwater.easybot.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

public class SkinUtils {
    private static final Gson gson = new Gson();

    /**
     * 从 ServerPlayer 中提取皮肤 URL
     * 原理：获取 GameProfile -> 读取 textures 属性 -> Base64解码 -> 解析JSON -> 提取 url 字段
     */
    public static String getSkinUrl(ServerPlayer player) {
        try {
            GameProfile profile = player.getGameProfile();
            // 获取 "textures" 属性
            //? if >1.21.8 {
            Collection<Property> textures = profile.properties().get("textures");
             //?} else {
            /*Collection<Property> textures = profile.getProperties().get("textures");
            *///?}
            if (!textures.isEmpty()) {
                Property property = textures.iterator().next(); // 获取第一个属性
                //? if >=1.20.2 {
                String value = property.value();
                 //?} else {
                /*String value = property.getValue();
                *///?}
                String decodedValue = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                // JSON 结构: {"timestamp":..., "profileId":..., "profileName":..., "textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/..."}}}
                JsonObject jsonObject = gson.fromJson(decodedValue, JsonObject.class);

                if (jsonObject.has("textures")) {
                    JsonObject texturesObj = jsonObject.getAsJsonObject("textures");
                    if (texturesObj.has("SKIN")) {
                        JsonObject skinObj = texturesObj.getAsJsonObject("SKIN");
                        if (skinObj.has("url")) {
                            return skinObj.get("url").getAsString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            ModData.LOGGER.error("获取皮肤URL时出错: {}", e.getMessage());
        }
        ModData.LOGGER.warn("获取皮肤URL时出错,已显示默认皮肤");
        // 小彩蛋说是
        // 获取失败
        return "http://textures.minecraft.net/texture/284e8bbc52bcc9513ee0bc84de84c34ec44454b0e0174c8df5694cc115c14b8d";
    }
}
