package dev.phonis.sharedwaypoints.server.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.phonis.sharedwaypoints.server.commands.argument.PositionCommandArgument;
import dev.phonis.sharedwaypoints.server.commands.argument.WaypointCommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import dev.phonis.sharedwaypoints.server.commands.internal.OptionalPairServerCommand;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import dev.phonis.sharedwaypoints.server.waypoints.Waypoint;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class CommandWaypointUpdate extends OptionalPairServerCommand<String, Coordinates>
{

    public CommandWaypointUpdate()
    {
        super("update", new WaypointCommandArgument("waypoint"), new PositionCommandArgument("position"));
        this.addAlias("u");
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source) throws CommandException
    {
        throw new CommandException("You must provide a waypoint name.");
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source, String s, Coordinates posArgument)
        throws CommandException, CommandSyntaxException
    {
        this.onOptionalCommand(source, s, posArgument.getPosition(source.getSource()), source.getSource().getPlayer()
            .level());
    }

    @Override
    protected void onOptionalCommand(CommandContext<CommandSourceStack> source, String s)
        throws CommandException, CommandSyntaxException
    {
        ServerPlayer player = source.getSource().getPlayer();

        this.onOptionalCommand(source, s, player.position(), player.level());
    }

    private void onOptionalCommand(CommandContext<CommandSourceStack> source, String s, Vec3 position,
                                   ServerLevel world) throws CommandException
    {
        if (!WaypointManager.INSTANCE.hasWaypoint(s))
        {
            throw new CommandException("Invalid waypoint to update.");
        }

        Waypoint waypoint = WaypointManager.INSTANCE.updateWaypoint(s, position, world);

        ContextUtil.sendMessage(source,
            ChatFormatting.WHITE + "Position of '" + ChatFormatting.AQUA + waypoint.getName() + ChatFormatting.WHITE + "' ➤ " +
            ChatFormatting.AQUA + waypoint.getWorld() + ChatFormatting.WHITE + " ➤ " + ChatFormatting.GRAY + (int) waypoint.getX() +
            ", " + (int) waypoint.getY() + ", " + (int) waypoint.getZ());
    }

}
