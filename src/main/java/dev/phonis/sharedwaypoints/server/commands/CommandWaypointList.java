package dev.phonis.sharedwaypoints.server.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.phonis.sharedwaypoints.server.commands.internal.NoArgServerCommand;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

public class CommandWaypointList extends NoArgServerCommand
{

    public CommandWaypointList()
    {
        super("list");
        this.addAlias("l");
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source)
    {
        if (WaypointManager.INSTANCE.numWaypoints() == 0)
        {
            ContextUtil.sendMessage(source, ChatFormatting.RED + "Currently there are no waypoints.");

            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        WaypointManager.INSTANCE.forEachWaypoint((waypoint, isLast) ->
        {
            messageBuilder.append(ChatFormatting.AQUA).append(waypoint.getName()).append(ChatFormatting.WHITE).append(" ➤ ")
                .append(ChatFormatting.GRAY).append(waypoint.getWorld()).append(" ").append((int) waypoint.getX())
                .append(" ").append((int) waypoint.getY()).append(" ").append((int) waypoint.getZ());
            if (!isLast)
            {
                messageBuilder.append('\n');
            }
        });
        ContextUtil.sendMessage(source, messageBuilder.toString());
    }

}
