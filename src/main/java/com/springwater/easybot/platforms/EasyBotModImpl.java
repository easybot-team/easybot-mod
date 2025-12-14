package com.springwater.easybot.platforms;

//? fabric {
/*import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.platforms.fabric.FabricModImpl;
*///?}
//? neoforge {
import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.platforms.neoforge.NeoForgeModImpl;
 //?}
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.nio.file.Path;

public interface EasyBotModImpl {
    //? fabric {
    /*EasyBotModImpl INSTANCE = new FabricModImpl();
    *///?}
    //? neoforge {
    EasyBotModImpl INSTANCE = new NeoForgeModImpl();
     //?}
    
    String getLoaderName();

    boolean isModLoaded(String modId);
    Logger getLogger();
    Path getConfigDirectory();
    MinecraftServer getServer();
    BridgeClient getBridgeClient();
}
