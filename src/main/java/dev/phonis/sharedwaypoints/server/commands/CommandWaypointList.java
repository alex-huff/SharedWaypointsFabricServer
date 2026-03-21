package dev.phonis.sharedwaypoints.server.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.phonis.sharedwaypoints.server.commands.internal.NoArgServerCommand;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class CommandWaypointList extends NoArgServerCommand
{

    public CommandWaypointList()
    {
        super("list");
        this.addAlias("l");
    }

    @Override
    protected void onOptionalCommand(CommandContext<ServerCommandSource> source)
    {
        if (WaypointManager.INSTANCE.numWaypoints() == 0)
        {
            ContextUtil.sendMessage(source, Formatting.RED + "Currently there are no waypoints.");

            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        WaypointManager.INSTANCE.forEachWaypoint((waypoint, isLast) ->
        {
            messageBuilder.append(Formatting.AQUA).append(waypoint.getName()).append(Formatting.WHITE).append(" ➤ ")
                .append(Formatting.GRAY).append(waypoint.getWorld()).append(" ").append((int) waypoint.getX())
                .append(" ").append((int) waypoint.getY()).append(" ").append((int) waypoint.getZ());
            if (!isLast)
            {
                messageBuilder.append('\n');
            }
        });
        ContextUtil.sendMessage(source, messageBuilder.toString());
    }

}
