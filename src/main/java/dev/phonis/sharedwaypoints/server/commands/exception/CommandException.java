package dev.phonis.sharedwaypoints.server.commands.exception;

import net.minecraft.ChatFormatting;

public class CommandException extends Exception
{

    private static final String prefix = ChatFormatting.RED + "Command usage error " + ChatFormatting.GRAY + "➤ " +
                                         ChatFormatting.WHITE;

    public CommandException(String error)
    {
        super(CommandException.prefix + error);
    }

}
