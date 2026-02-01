package com.springwater.easybot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class EasyBotConfig {
    private String ws = "ws://127.0.0.1:26990/bridge";
    private boolean debug = false;
    private int reconnectInterval = 5000;
    private String token = "YOUR_TOKEN_HERE";
    private boolean ignoreError = false;
    private boolean updateNotify = true;
    private boolean enableWhiteList = false;
    
    private Message message = new Message();
    private Command command = new Command();
    private SkipOptions skipOptions = new SkipOptions();
    private Geyser geyser = new Geyser();
    private Event event = new Event();

    private Fabric fabric = new Fabric();
    
    @Getter
    @Setter
    @ToString
    public static class Fabric{
        private boolean useMixinReport1201 = false;
    }
    
    @Getter
    @Setter
    @ToString
    public static class Message {
        private String bindStart = "[!] 绑定开始,请加群12345678输入: \"绑定 #code\" 进行绑定, 请在#time完成绑定!";
        private String bindSuccess = "§f[§a!§f] 绑定§f §a#account §f(§a#name§f) 成功!";
        private String bindFail = "§f[§c!§f] §c绑定失败 #why";
    }

    @Getter
    @Setter
    @ToString
    public static class Command {
        private boolean allowBind = true;
        private int waitTime = 3;
    }

    @Getter
    @Setter
    @ToString
    public static class SkipOptions {
        private boolean skipJoin = false;
        private boolean skipQuit = false;
        private boolean skipChat = false;
        private boolean skipDeath = false;
    }

    @Getter
    @Setter
    @ToString
    public static class Geyser {
        private boolean ignorePrefix = false;
        private boolean useRealUuid = false;
    }
    
    @Getter
    @Setter
    @ToString
    public static class Event{
        private boolean enableSuccessEvent = false;
        private List<String> bindSuccess = new ArrayList<>(List.of("say 玩家$player绑定成功,Id=$account,账号名字=$name")); // 这是个默认值
    }
}