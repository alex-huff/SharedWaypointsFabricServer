package dev.phonis.sharedwaypoints.server.waypoints;

public interface WaypointsListener
{

    void onWaypointAdd(Waypoint waypoint);

    void onWaypointUpdate(Waypoint waypoint, String oldWorld);

    void onWaypointRemove(Waypoint waypoint);


}
