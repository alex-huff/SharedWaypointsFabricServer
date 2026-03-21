package dev.phonis.sharedwaypoints.server.interop.bluemap;

import com.google.common.html.HtmlEscapers;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import dev.phonis.sharedwaypoints.server.waypoints.Waypoint;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointsListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Optional;

public class BlueMapManager implements WaypointsListener
{

    public static final BlueMapManager INSTANCE = new BlueMapManager();

    private static final String markerHTML = """
                                             <div class="bm-marker-player">
                                                 <div class="bm-player-name" style="width: max-content;">marker-label</div>
                                             </div>
                                             """;

    public static String getMapIDFromWorldID(String worldID)
    {
        switch (worldID)
        {
            case WaypointManager.netherIdentifier ->
            {
                return "nether";
            }
            case WaypointManager.endIdentifier ->
            {
                return "end";
            }
            default ->
            {
                return "overworld";
            }
        }
    }

    public static String getMarkerSetIDFromWorldID(String dimension)
    {
        return dimension + "-waypoints";
    }

    public static Marker getMarkerFromWaypoint(Waypoint waypoint)
    {
        return HtmlMarker.builder().html(BlueMapManager.markerHTML.replace("marker-label", HtmlEscapers.htmlEscaper().escape(waypoint.getName())))
            .position(waypoint.getX(), waypoint.getY(), waypoint.getZ()).label(waypoint.getName()).build();
    }

    public static void createWaypoints(BlueMapAPI api)
    {
        MarkerSet waypointSetOverworld = MarkerSet.builder().defaultHidden(true)
            .label(BlueMapManager.getMarkerSetLabelFromWorldID(WaypointManager.overworldIdentifier)).build();
        MarkerSet waypointSetNether = MarkerSet.builder().defaultHidden(true)
            .label(BlueMapManager.getMarkerSetLabelFromWorldID(WaypointManager.netherIdentifier)).build();
        MarkerSet waypointSetEnd = MarkerSet.builder().defaultHidden(true)
            .label(BlueMapManager.getMarkerSetLabelFromWorldID(WaypointManager.endIdentifier)).build();
        WaypointManager.INSTANCE.forEachWaypoint((waypoint) ->
        {
            MarkerSet markerSet;
            switch (waypoint.getWorld())
            {
                case WaypointManager.netherIdentifier -> markerSet = waypointSetNether;
                case WaypointManager.endIdentifier -> markerSet = waypointSetEnd;
                default -> markerSet = waypointSetOverworld;
            }
            markerSet.getMarkers().put(waypoint.getName(), getMarkerFromWaypoint(waypoint));
        });
        BlueMapManager.addBlueMapMarkerSetToWorld(api, WaypointManager.overworldIdentifier, waypointSetOverworld);
        BlueMapManager.addBlueMapMarkerSetToWorld(api, WaypointManager.netherIdentifier, waypointSetNether);
        BlueMapManager.addBlueMapMarkerSetToWorld(api, WaypointManager.endIdentifier, waypointSetEnd);
    }

    private static void addBlueMapMarkerSetToWorld(BlueMapAPI api, String worldID, MarkerSet markerSet)
    {
        api.getMap(getMapIDFromWorldID(worldID))
            .ifPresent(map -> map.getMarkerSets().put(getMarkerSetIDFromWorldID(worldID), markerSet));
    }

    private static String getMarkerSetLabelFromWorldID(String worldID)
    {
        switch (worldID)
        {
            case WaypointManager.netherIdentifier ->
            {
                return "Nether Waypoints";
            }
            case WaypointManager.endIdentifier ->
            {
                return "End Waypoints";
            }
            default ->
            {
                return "Overworld Waypoints";
            }
        }
    }

    public static Optional<BlueMapMap> getMap(BlueMapAPI api, String world)
    {
        return api.getMap(BlueMapManager.getMapIDFromWorldID(world));
    }

    public static MarkerSet getMarkerSet(BlueMapMap map, String world)
    {
        return map.getMarkerSets().get(BlueMapManager.getMarkerSetIDFromWorldID(world));
    }

    public void registerBlueMap()
    {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> BlueMapAPI.onEnable((api) -> server.execute(() -> BlueMapManager.createWaypoints(api))));
        WaypointManager.INSTANCE.addWaypointsListeners(this);
    }

    @Override
    public void onWaypointAdd(Waypoint waypoint)
    {
        BlueMapAPI.getInstance().ifPresent(api ->
        {
            BlueMapManager.getMap(api, waypoint.getWorld())
                .ifPresent((map) -> BlueMapManager.getMarkerSet(map, waypoint.getWorld())
                    .put(waypoint.getName(), BlueMapManager.getMarkerFromWaypoint(waypoint)));
        });
    }

    @Override
    public void onWaypointUpdate(Waypoint waypoint, String oldWorld)
    {
        BlueMapAPI.getInstance().ifPresent(api ->
        {
            if (!oldWorld.equals(waypoint.getWorld()))
            {
                BlueMapManager.getMap(api, oldWorld)
                    .ifPresent(map -> BlueMapManager.getMarkerSet(map, oldWorld)
                        .remove(waypoint.getName()));
            }
            BlueMapManager.getMap(api, waypoint.getWorld()).ifPresent((map) -> BlueMapManager.getMarkerSet(map, waypoint.getWorld())
                .put(waypoint.getName(), BlueMapManager.getMarkerFromWaypoint(waypoint)));
        });
    }

    @Override
    public void onWaypointRemove(Waypoint waypoint)
    {
        BlueMapAPI.getInstance().ifPresent(api ->
        {
            BlueMapManager.getMap(api, waypoint.getWorld()).ifPresent((map) -> BlueMapManager.getMarkerSet(map, waypoint.getWorld())
                .remove(waypoint.getName()));
        });
    }

}
