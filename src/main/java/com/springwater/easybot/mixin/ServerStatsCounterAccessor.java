package com.springwater.easybot.mixin;
//? >= 1.21.11 {
/*import com.google.gson.JsonElement;
*///?}
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatsCounter.class)
public interface ServerStatsCounterAccessor {
    //? >= 1.21.11 {
    /*@Invoker("toJson")
    JsonElement easybot$toJson();
    *///?} else {
    @Invoker("toJson")
    String easybot$toJson();
    //?}
}
