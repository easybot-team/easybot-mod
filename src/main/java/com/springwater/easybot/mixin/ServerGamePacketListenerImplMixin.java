package com.springwater.easybot.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


//? legacyforge {
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.event.ServerChatEvent;
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Inject(method = "broadcastChatMessage", at = @At("HEAD"))
    private void onBroadcastChatMessage(PlayerChatMessage message, CallbackInfo ci) {
        var player = ((ServerGamePacketListenerImpl) (Object) this).player;
        if (!message.isSystem()) {
            var event = new ServerChatEvent(player, message.signedContent(), message.decoratedContent());
            com.springwater.easybot.platforms.legacyforge.features.MessageSyncFeature.onChatMessage(event);
        }
    }
}
//?} else {
/*import net.minecraft.server.MinecraftServer;
@Mixin(MinecraftServer.class)
public class ServerGamePacketListenerImplMixin {
}
*///?}