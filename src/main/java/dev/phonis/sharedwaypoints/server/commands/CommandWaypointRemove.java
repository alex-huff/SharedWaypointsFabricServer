package dev.phonis.sharedwaypoints.server.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.phonis.sharedwaypoints.server.commands.argument.WaypointCommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import dev.phonis.sharedwaypoints.server.commands.internal.OptionalSingleServerCommand;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import dev.phonis.sharedwaypoints.server.waypoints.Waypoint;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

public class CommandWaypointRemove extends OptionalSingleServerCommand<String>
{

    public CommandWaypointRemove()
    {
        super("remove", new WaypointCommandArgument("waypoint"));
        this.addAlias("r");
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source) throws CommandException
    {
        throw new CommandException("You must provide a waypoint name.");
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source, String s) throws CommandException
    {
        Waypoint waypoint = WaypointManager.INSTANCE.removeWaypoint(s);

        if (waypoint == null)
        {
            throw new CommandException("Invalid waypoint for removal.");
        }

        ContextUtil.sendMessage(source,
            ChatFormatting.WHITE + "Waypoint '" + ChatFormatting.AQUA + waypoint.getName() + ChatFormatting.WHITE + "' removed.");
    }

}
