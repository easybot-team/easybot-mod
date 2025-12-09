package com.springwater.easybot.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.springwater.easybot.commands.handlers.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

public class EasyBotCommands {
    private static final List<ICommandHandler> COMMAND_HANDLERS = List.of(
            new HelpCommandHandler(),
            new StatusCommandHandler(),
            new SayCommandHandler(),
            new ReloadCommandHandler(),
            new BindCommandHandler()
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("easybot");
        COMMAND_HANDLERS.forEach(handler -> handler.register(root));
        dispatcher.register(root);
    }
}
