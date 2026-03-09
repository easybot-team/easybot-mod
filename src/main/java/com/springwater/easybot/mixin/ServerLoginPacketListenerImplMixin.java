package com.springwater.easybot.mixin;
//? 1.20.1 {

/*import com.mojang.authlib.GameProfile;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;
import com.springwater.easybot.platforms.common.CommonPlayerLoginFeature;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
    @Shadow
    public abstract void disconnect(Component component);

    @Shadow
    GameProfile gameProfile;

    @Shadow
    protected abstract GameProfile createFakeProfile(GameProfile par1);

    @Unique
    private boolean easybot$handled = false;
    @Unique
    private int easybot$tickCount = 0;

    @Unique
    private CompletableFuture<Object> easybot$future = null;

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;handleAcceptedLogin()V"), cancellable = true)
    private void easybot$injectedTick(CallbackInfo ci) {
        if (easybot$tickCount == 0 && easybot$future == null) {
            //? legacyforge {
            /^if (true) {
                ^///?} else {
                if (ConfigLoader.get().getFabric().isUseMixinReport1201()) {
                 //?}
                if (CommonPlayerLoginFeature.getHandler() == null) {
                    if (!ConfigLoader.get().isIgnoreError()) disconnect(Component.literal("§c[EasyBot] 服务器内部初始化错误,请检查日志!"));
                    ModData.LOGGER.error("fabric.isUseMixinReport但未初始化通用接口,该版本可能并不支持此模式,请尝试恢复false!");
                    easybot$handled = true;
                    return;
                }

                if (!this.gameProfile.isComplete()) {
                    this.gameProfile = createFakeProfile(this.gameProfile);
                }

                easybot$future = CompletableFuture.supplyAsync(() -> {
                    CommonPlayerLoginFeature.getHandler().onLoginStart((ServerLoginPacketListenerImpl) (Object) this);
                    return null;
                });
                easybot$future.thenAccept(v -> easybot$handled = true);
                easybot$future.exceptionally(e -> {
                    ModData.LOGGER.error(e.toString());
                    if (!ConfigLoader.get().isIgnoreError()) disconnect(Component.literal("§c服务器内部异常,请稍后再试!"));
                    return null;
                });
            } else {
                easybot$handled = true;
            }
        }

        if (!easybot$handled) {
            easybot$tickCount++;
            if (easybot$tickCount >= 20 * 5) {
                disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
            }
            ci.cancel();
        }
    }

}
*///?} else {
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {}
//?}