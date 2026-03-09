package com.springwater.easybot.impl;

import net.minecraft.network.chat.*;

import java.net.URI;

public class ComponentAdapterImpl {
    public static Style withHoverText(Style style, Component hoverText) {
        //? if >=1.21.6 {
        return style.withHoverEvent(
                new HoverEvent.ShowText(hoverText)
        );
        //?} else {
        /*return style.withHoverEvent(
            new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)
        );
        *///?}
    }

    public static Style withOpenUrl(Style style, String url) {
        //? if >=1.21.6 {
        return style.withClickEvent(
                new ClickEvent.OpenUrl(URI.create(url))
        );
        //?} else {
        /*return style.withClickEvent(
            new ClickEvent(ClickEvent.Action.OPEN_URL, url)
        );
        *///?}
    }

}
