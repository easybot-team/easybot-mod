package com.springwater.easybot.commands.handlers;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.springwater.easybot.commands.ICommandHandler;
import com.springwater.easybot.platforms.ModData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class HelpCommandHandler implements ICommandHandler {

    @Override
    public void register(LiteralArgumentBuilder<CommandSourceStack> stack) {
        stack
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("help").executes(this::sendGetHelp))
                .executes(this::sendGetHelp);
    }

    private int sendGetHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        MutableComponent root = Component.empty();
        root.append(Component.literal("--------------------------------------------------")
                .withStyle(ChatFormatting.GRAY));
        root.append(Component.literal("\n EasyBot Fabric V")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(ModData.VERSION)
                        .withStyle(ChatFormatting.GREEN)));
        root.append(buildCommandLine("/easybot help", "获取帮助"));
        root.append(buildCommandLine("/easybot say <消息>", "主动消息同步"));
        root.append(buildCommandLine("/easybot status", "获取状态,需OP"));
        root.append(buildCommandLine("/easybot reload", "重载配置,需OP"));
        root.append(Component.literal("\n--------------------------------------------------")
                .withStyle(ChatFormatting.GRAY));
        source.sendSuccess(() -> root, false);
        return 1;
    }

    private MutableComponent buildCommandLine(String command, String description) {
        return Component.literal("\n")
                .append(Component.literal(command)
                        .withStyle(ChatFormatting.DARK_GREEN))
                .append(Component.literal(" - ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(description)
                        .withStyle(ChatFormatting.DARK_GRAY));
    }
}