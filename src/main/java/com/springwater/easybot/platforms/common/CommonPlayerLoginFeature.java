package com.springwater.easybot.platforms.common;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

public class CommonPlayerLoginFeature {
    @Getter
    @Setter
    private static QueryStart handler = null;

    public interface QueryStart {
        void onLoginStart(ServerLoginPacketListenerImpl handler);
    }
}
