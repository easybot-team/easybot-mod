package com.springwater.easybot.utils;

import net.minecraft.commands.CommandSourceStack;
//? >= 1.21.11 {
/*import net.minecraft.server.permissions.Permissions;
*///?}
public class PermissionUtils {
    public static boolean hasPermission(CommandSourceStack commandSourceStack, int permissionLevel) {
        if (permissionLevel == 0) return true;
        //? >= 1.21.11 {
        /*return commandSourceStack.permissions().hasPermission(
                switch (permissionLevel) {
                    case 1 -> Permissions.COMMANDS_MODERATOR;
                    case 2 -> Permissions.COMMANDS_GAMEMASTER;
                    case 3 -> Permissions.COMMANDS_ADMIN;
                    case 4 -> Permissions.COMMANDS_OWNER;
                    default -> throw new IllegalStateException("Unexpected value: " + permissionLevel);
                }
        );
        *///?} else {
        return commandSourceStack.hasPermission(permissionLevel);
         //?}
    }
}
