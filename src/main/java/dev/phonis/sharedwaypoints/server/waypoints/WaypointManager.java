package dev.phonis.sharedwaypoints.server.waypoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import dev.phonis.sharedwaypoints.server.SharedWaypointsServer;
import dev.phonis.sharedwaypoints.server.networking.SWNetworkManager;
import dev.phonis.sharedwaypoints.server.networking.protocol.action.SWWaypointRemoveAction;
import dev.phonis.sharedwaypoints.server.networking.protocol.action.SWWaypointUpdateAction;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WaypointManager
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String waypointFile = SharedWaypointsServer.configDirectory + "waypoints.json";
    public static final String backupDirectory = SharedWaypointsServer.configDirectory + "backup/";
    public static final WaypointManager INSTANCE = WaypointManager.load();
    public static final String overworldIdentifier = "overworld";
    public static final String netherIdentifier = "the_nether";
    public static final String endIdentifier = "the_end";

    private final Map<String, Waypoint> waypointMap = new HashMap<>();
    private final List<WaypointsListener> waypointsListeners = new ArrayList<>();

    public void addWaypointsListeners(WaypointsListener waypointsListener)
    {
        this.waypointsListeners.add(waypointsListener);
    }

    public void forEachWaypoint(Consumer<Waypoint> consumer)
    {
        this.waypointMap.values().forEach(consumer);
    }

    public void forEachWaypoint(BiConsumer<Waypoint, Boolean> consumer)
    {
        Collection<Waypoint> values = this.waypointMap.values();
        Iterator<Waypoint> iterator = values.iterator();

        while (iterator.hasNext())
        {
            consumer.accept(iterator.next(), !iterator.hasNext());
        }
    }

    public Waypoint getWaypoint(String name)
    {
        return this.waypointMap.get(name);
    }

    public Waypoint removeWaypoint(String name)
    {
        Waypoint waypoint = this.waypointMap.remove(name);

        if (waypoint != null)
        {
            for (WaypointsListener waypointsListener : this.waypointsListeners)
            {
                waypointsListener.onWaypointRemove(waypoint);
            }
            SWNetworkManager.INSTANCE.sendToSubscribed(new SWWaypointRemoveAction(waypoint.getName()));

            this.trySave();
        }

        return waypoint;
    }

    public Waypoint addWaypoint(CommandContext<ServerCommandSource> source, String name)
    {
        Vec3d position = source.getSource().getPosition();
        World world = source.getSource().getWorld();
        String worldString = world.getRegistryKey().getValue().getPath();
        Waypoint waypoint = this.getWaypoint(name);

        if (waypoint == null)
        {
            waypoint = new Waypoint(name, worldString, position.getX(), position.getY(), position.getZ());
            this.waypointMap.put(name, waypoint);
            for (WaypointsListener waypointsListener : this.waypointsListeners)
            {
                waypointsListener.onWaypointAdd(waypoint);
            }
        }
        else
        {
            String oldWorldString = waypoint.getWorld();
            waypoint.update(position, worldString);
            for (WaypointsListener waypointsListener : this.waypointsListeners)
            {
                waypointsListener.onWaypointUpdate(waypoint, oldWorldString);
            }
        }
        SWNetworkManager.INSTANCE.sendToSubscribed(new SWWaypointUpdateAction(waypoint));

        this.trySave();

        return waypoint;
    }

    public Waypoint updateWaypoint(String name, Vec3d position, ServerWorld world)
    {
        Waypoint waypoint = this.waypointMap.get(name);

        if (waypoint != null)
        {
            String oldWorldString = waypoint.getWorld();
            waypoint.update(position, world.getRegistryKey().getValue().getPath());
            for (WaypointsListener waypointsListener : this.waypointsListeners)
            {
                waypointsListener.onWaypointUpdate(waypoint, oldWorldString);
            }
            SWNetworkManager.INSTANCE.sendToSubscribed(new SWWaypointUpdateAction(waypoint));

            this.trySave();
        }

        return waypoint;
    }

    public boolean hasWaypoint(String name)
    {
        return this.waypointMap.containsKey(name);
    }

    public int numWaypoints()
    {
        return this.waypointMap.size();
    }

    private static WaypointManager load()
    {
        if (Files.exists(Path.of(WaypointManager.waypointFile)))
        {
            try (FileReader reader = new FileReader(WaypointManager.waypointFile))
            {
                WaypointManager.backup();

                return GSON.fromJson(reader, WaypointManager.class);
            }
            catch (IOException | JsonSyntaxException e)
            {
                System.out.println("Could not read waypoints.");
            }
        }

        return new WaypointManager();
    }

    public static void backup() throws IOException
    {
        Path path = Path.of(WaypointManager.waypointFile);
        Path backupPath = Path.of(
            WaypointManager.backupDirectory + path.getFileName() + UUID.randomUUID().toString().replaceAll("-", "") +
            ".backup");
        Path parent = backupPath.getParent();

        if (!Files.exists(parent))
        {
            Files.createDirectories(parent);
        }

        Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public void saveToFile() throws IOException
    {
        Path path = Path.of(WaypointManager.waypointFile);
        Path parent = path.getParent();

        if (!Files.exists(parent))
        {
            Files.createDirectories(parent);
        }

        // Atomic file replace
        Path tempPath = path.resolveSibling(path.getFileName() + ".tmp");

        Files.writeString(tempPath, GSON.toJson(this));
        Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    public void trySave()
    {
        try
        {
            this.saveToFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
