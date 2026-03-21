package dev.phonis.sharedwaypoints.server.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.phonis.sharedwaypoints.server.commands.argument.StringCommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import dev.phonis.sharedwaypoints.server.commands.internal.OptionalSingleServerCommand;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import dev.phonis.sharedwaypoints.server.waypoints.Waypoint;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class CommandWaypointSet extends OptionalSingleServerCommand<String>
{

    public CommandWaypointSet()
    {
        super("set", new StringCommandArgument("name"));
        this.addAlias("s");
    }

    @Override
    protected void onOptionalCommand(CommandContext<ServerCommandSource> source) throws CommandException
    {
        throw new CommandException("You must provide a waypoint name.");
    }

    @Override
    protected void onOptionalCommand(CommandContext<ServerCommandSource> source, String s)
    {
        Waypoint waypoint = WaypointManager.INSTANCE.addWaypoint(source, s);

        ContextUtil.sendMessage(source,
            Formatting.WHITE + "Waypoint '" + Formatting.AQUA + waypoint.getName() + Formatting.WHITE + "' ➤ " +
            Formatting.GRAY + waypoint.getWorld() + " " + Formatting.GRAY + (int) waypoint.getX() + " " +
            (int) waypoint.getY() + " " + (int) waypoint.getZ());
    }

}
