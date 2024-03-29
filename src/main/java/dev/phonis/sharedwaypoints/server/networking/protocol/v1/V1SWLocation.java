package dev.phonis.sharedwaypoints.server.networking.protocol.v1;

import dev.phonis.sharedwaypoints.server.networking.protocol.persistant.SWSerializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public
class V1SWLocation implements SWSerializable
{

    public final V1SWDimension dimension;
    public final double        x;
    public final double        y;
    public final double        z;

    public
    V1SWLocation(V1SWDimension dimension, double x, double y, double z)
    {
        this.dimension = dimension;
        this.x         = x;
        this.y         = y;
        this.z         = z;
    }

    @Override
    public
    boolean equals(Object other)
    {
        if (other instanceof V1SWLocation otherLocation)
        {
            return this.dimension.equals(otherLocation.dimension) && this.x == otherLocation.x &&
                   this.y == otherLocation.y && this.z == otherLocation.z;
        }

        return false;
    }

    @Override
    public
    void toBytes(DataOutputStream dos) throws IOException
    {
        dos.writeByte(this.dimension.ordinal());
        dos.writeDouble(this.x);
        dos.writeDouble(this.y);
        dos.writeDouble(this.z);
    }

    public static
    V1SWLocation fromBytes(DataInputStream dis) throws IOException
    {
        return new V1SWLocation(V1SWDimension.fromBytes(dis), dis.readDouble(), dis.readDouble(), dis.readDouble());
    }

}