package dev.phonis.sharedwaypoints.server.waypoints;

import net.minecraft.world.phys.Vec3;

public class Waypoint
{

    private final String name;
    private String world;
    private double xPos;
    private double yPos;
    private double zPos;

    public Waypoint(String name, String world, double xPos, double yPos, double zPos)
    {
        this.name = name;
        this.world = world;
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }

    public String getName()
    {
        return this.name;
    }

    public String getWorld()
    {
        return this.world;
    }

    public double getX()
    {
        return this.xPos;
    }

    public double getY()
    {
        return this.yPos;
    }

    public double getZ()
    {
        return this.zPos;
    }

    public void update(Vec3 position, String world)
    {
        this.xPos = position.x;
        this.yPos = position.y;
        this.zPos = position.z;
        this.world = world;
    }

}
