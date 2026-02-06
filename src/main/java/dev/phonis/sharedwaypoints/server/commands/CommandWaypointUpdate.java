package dev.phonis.sharedwaypoints.server.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.bluecolored.bluemap.api.BlueMapAPI;
import dev.phonis.sharedwaypoints.server.SharedWaypointsServer;
import dev.phonis.sharedwaypoints.server.commands.argument.PositionCommandArgument;
import dev.phonis.sharedwaypoints.server.commands.argument.WaypointCommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import dev.phonis.sharedwaypoints.server.commands.internal.OptionalPairServerCommand;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import dev.phonis.sharedwaypoints.server.map.BlueMapHelper;
import dev.phonis.sharedwaypoints.server.map.DynmapHelper;
import dev.phonis.sharedwaypoints.server.networking.SWNetworkManager;
import dev.phonis.sharedwaypoints.server.networking.protocol.action.SWWaypointUpdateAction;
import dev.phonis.sharedwaypoints.server.waypoints.Waypoint;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class CommandWaypointUpdate extends OptionalPairServerCommand<String, PosArgument>
{

    public CommandWaypointUpdate()
    {
        super("update", new WaypointCommandArgument("waypoint"), new PositionCommandArgument("position"));
        this.addAlias("u");
    }

    @Override
    protected void onOptionalCommand(CommandContext<ServerCommandSource> source) throws CommandException
    {
        throw new CommandException("You must provide a waypoint name.");
    }

    @Override
    protected void onOptionalCommand(CommandContext<ServerCommandSource> source, String s, PosArgument posArgument)
        throws CommandException, CommandSyntaxException
    {
        this.onOptionalCommand(source, s, posArgument.getPos(source.getSource()), source.getSource().getPlayer()
            .getEntityWorld());
    }

    @Override
    protected void onOptionalCommand(CommandContext<ServerCommandSource> source, String s)
        throws CommandException, CommandSyntaxException
    {
        ServerPlayerEntity player = source.getSource().getPlayer();

        this.onOptionalCommand(source, s, player.getEntityPos(), player.getEntityWorld());
    }

    private void onOptionalCommand(CommandContext<ServerCommandSource> source, String s, Vec3d position,
                                   ServerWorld world) throws CommandException
    {
        if (!WaypointManager.INSTANCE.hasWaypoint(s))
        {
            throw new CommandException("Invalid waypoint to update.");
        }

        String oldWaypointWorld = WaypointManager.INSTANCE.getWaypoint(s).getWorld();
        Waypoint waypoint = WaypointManager.INSTANCE.updateWaypoint(s, position, world);

        ContextUtil.sendMessage(source,
            Formatting.WHITE + "Position of '" + Formatting.AQUA + waypoint.getName() + Formatting.WHITE + "' ➤ " +
            Formatting.AQUA + waypoint.getWorld() + Formatting.WHITE + " ➤ " + Formatting.GRAY + (int) waypoint.getX() +
            ", " + (int) waypoint.getY() + ", " + (int) waypoint.getZ());
        if (!oldWaypointWorld.equals(waypoint.getWorld()))
        {
            BlueMapAPI.getInstance().flatMap(api -> api.getMap(BlueMapHelper.getMapIDFromWorldID(oldWaypointWorld)))
                .ifPresent((map) -> map.getMarkerSets().get(BlueMapHelper.getMarkerSetIDFromWorldID(oldWaypointWorld))
                    .remove(waypoint.getName()));
        }
        BlueMapAPI.getInstance().flatMap(api -> api.getMap(BlueMapHelper.getMapIDFromWorldID(waypoint.getWorld())))
            .ifPresent((map) -> map.getMarkerSets().get(BlueMapHelper.getMarkerSetIDFromWorldID(waypoint.getWorld()))
                .put(waypoint.getName(), BlueMapHelper.getMarkerFromWaypoint(waypoint)));
        SharedWaypointsServer.getDynmapAPI()
            .ifPresent(api -> api.getMarkerAPI().getMarkerSet(DynmapHelper.markerSetID).findMarker(waypoint.getName())
                .setLocation(DynmapHelper.getDynmapWorldIDFromSWWorldID(waypoint.getWorld()), waypoint.getX(), waypoint.getY(), waypoint.getZ()));
        SWNetworkManager.INSTANCE.sendToSubscribed(source, new SWWaypointUpdateAction(waypoint));
    }

}
