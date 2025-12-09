package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.utils.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandImpl {
    public String DispatchCommand(String command) {
        var source = new CommandSourceImpl();
        var level = EasyBotFabric.getServer().overworld();
        CommandSourceStack stack = new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, level, 4, "EasyBotCommandDispatcher", Component.literal("EasyBotCommandDispatcher"), EasyBotFabric.getServer(), null);
        EasyBotFabric.getServer().getCommands().performPrefixedCommand(stack, command);
        var messages = source.getMessages();
        return String.join("\n", messages.stream().map(TextUtils::toLegacyString).toArray(String[]::new));
    }
}
