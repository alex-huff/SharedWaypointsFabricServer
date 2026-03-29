package dev.phonis.sharedwaypoints.server.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.phonis.sharedwaypoints.server.commands.argument.StringCommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import dev.phonis.sharedwaypoints.server.commands.internal.OptionalSingleServerCommand;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import dev.phonis.sharedwaypoints.server.waypoints.Waypoint;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

public class CommandWaypointSet extends OptionalSingleServerCommand<String>
{

    public CommandWaypointSet()
    {
        super("set", new StringCommandArgument("name"));
        this.addAlias("s");
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source) throws CommandException
    {
        throw new CommandException("You must provide a waypoint name.");
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source, String s)
    {
        Waypoint waypoint = WaypointManager.INSTANCE.addWaypoint(source, s);

        ContextUtil.sendMessage(source,
            ChatFormatting.WHITE + "Waypoint '" + ChatFormatting.AQUA + waypoint.getName() + ChatFormatting.WHITE + "' ➤ " +
            ChatFormatting.GRAY + waypoint.getWorld() + " " + ChatFormatting.GRAY + (int) waypoint.getX() + " " +
            (int) waypoint.getY() + " " + (int) waypoint.getZ());
    }

}
