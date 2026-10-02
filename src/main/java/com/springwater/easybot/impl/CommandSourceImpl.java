package com.springwater.easybot.impl;

import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CommandSourceImpl implements CommandSource {
    private final List<Component> messages = new ArrayList<>();

    public synchronized List<Component> getMessages() {
        return List.copyOf(messages);
    }

    @Override
    public synchronized void sendSystemMessage(Component component) {
        messages.add(component);
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }
}
