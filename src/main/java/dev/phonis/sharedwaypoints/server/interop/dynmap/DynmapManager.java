package dev.phonis.sharedwaypoints.server.interop.dynmap;

import dev.phonis.sharedwaypoints.server.waypoints.Waypoint;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointManager;
import dev.phonis.sharedwaypoints.server.waypoints.WaypointsListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.Optional;

public class DynmapManager implements WaypointsListener
{

    public static final DynmapManager INSTANCE = new DynmapManager();

    public static final String markerSetID = "dynmap-waypoints";

    public static final String markerSetLabel = "Waypoints";
    public static DynmapCommonAPI dynmapAPI = null;

    public static String getDynmapWorldIDFromSWWorldID(String swWorldID)
    {
        switch (swWorldID)
        {
            case WaypointManager.netherIdentifier ->
            {
                return "DIM-1";
            }
            case WaypointManager.endIdentifier ->
            {
                return "DIM1";
            }
            default ->
            {
                return "world";
            }
        }
    }

    public static void createMarkerFromWaypoint(Waypoint waypoint, MarkerSet markerSet)
    {
        Marker existingMarker = markerSet.findMarker(waypoint.getName());
        if (existingMarker != null)
        {
            existingMarker.deleteMarker();
        }
        markerSet.createMarker(waypoint.getName(), waypoint.getName(), DynmapManager.getDynmapWorldIDFromSWWorldID(waypoint.getWorld()), waypoint.getX(), waypoint.getY(), waypoint.getZ(), markerSet.getDefaultMarkerIcon(), false);
    }

    public static Optional<DynmapCommonAPI> getDynmapAPI()
    {
        return Optional.ofNullable(dynmapAPI);
    }

    public static void initializeAndCreateWaypoints(DynmapCommonAPI api)
    {
        DynmapManager.dynmapAPI = api;
        MarkerAPI markerAPI = DynmapManager.dynmapAPI.getMarkerAPI();
        MarkerSet waypoints
            = markerAPI.createMarkerSet(markerSetID, markerSetLabel, null, false);
        waypoints.setHideByDefault(true);
        waypoints.setDefaultMarkerIcon(markerAPI.getMarkerIcon("blueflag"));
        WaypointManager.INSTANCE.forEachWaypoint((waypoint) -> DynmapManager.createMarkerFromWaypoint(waypoint, waypoints));
    }

    public static MarkerSet getMarkerSet(DynmapCommonAPI api)
    {
        return api.getMarkerAPI().getMarkerSet(DynmapManager.markerSetID);
    }

    public static Marker getMarker(DynmapCommonAPI api, Waypoint waypoint)
    {
        return DynmapManager.getMarkerSet(api).findMarker(waypoint.getName());
    }

    public void registerDynmap()
    {
        ServerLifecycleEvents.SERVER_STARTED.register(
            // Dynmap might always call apiEnabled on the main thread, in
            // which case I don't actually need to user MinecraftServer::execute to run the task later on the main
            // thread.
            (server) -> DynmapCommonAPIListener.register(new DynmapCommonAPIListener()
            {
                @Override
                public void apiEnabled(DynmapCommonAPI api)
                {
                    server.execute(() -> DynmapManager.initializeAndCreateWaypoints(api));
                }
            }));
        WaypointManager.INSTANCE.addWaypointsListeners(this);
    }

    @Override
    public void onWaypointAdd(Waypoint waypoint)
    {
        DynmapManager.getDynmapAPI()
            .ifPresent(api -> DynmapManager.createMarkerFromWaypoint(waypoint, DynmapManager.getMarkerSet(api)));
    }

    @Override
    public void onWaypointUpdate(Waypoint waypoint, String oldWorld)
    {
        DynmapManager.getDynmapAPI()
            .ifPresent(api -> DynmapManager.getMarker(api, waypoint)
                .setLocation(DynmapManager.getDynmapWorldIDFromSWWorldID(waypoint.getWorld()), waypoint.getX(), waypoint.getY(), waypoint.getZ()));
    }

    @Override
    public void onWaypointRemove(Waypoint waypoint)
    {
        DynmapManager.getDynmapAPI().ifPresent(api -> DynmapManager.getMarker(api, waypoint).deleteMarker());
    }

}
