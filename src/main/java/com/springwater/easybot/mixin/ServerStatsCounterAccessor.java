package com.springwater.easybot.mixin;

import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatsCounter.class)
public interface ServerStatsCounterAccessor {
    @Invoker("toJson") 
    String easybot$toJson();
}
