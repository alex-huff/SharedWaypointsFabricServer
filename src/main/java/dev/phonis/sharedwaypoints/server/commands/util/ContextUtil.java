package dev.phonis.sharedwaypoints.server.commands.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ContextUtil
{

    public static void sendMessage(CommandContext<CommandSourceStack> context, String message)
    {
        context.getSource().sendSystemMessage(Component.nullToEmpty(message));
    }

}
