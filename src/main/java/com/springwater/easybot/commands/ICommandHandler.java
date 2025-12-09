package com.springwater.easybot.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface ICommandHandler {
    void register(LiteralArgumentBuilder<CommandSourceStack> stack);
}
