package com.springwater.easybot.mixin;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.net.SocketAddress;

//? legacyforge {
/*import com.springwater.easybot.platforms.legacyforge.features.PlayerLoginFeature;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.network.chat.Component;
@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true)
    private void interceptLogin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Component> cir) {
        var kickReason = PlayerLoginFeature.checkLoginSync(profile, address);
        if (kickReason != null) {
            cir.setReturnValue(kickReason);
        }
    }
}
*///?} else {
import net.minecraft.server.MinecraftServer;
@Mixin(PlayerList.class)
public class PlayerListMixin {
}
//?}