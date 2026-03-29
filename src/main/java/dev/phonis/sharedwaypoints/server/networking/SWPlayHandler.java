package dev.phonis.sharedwaypoints.server.networking;

import dev.phonis.sharedwaypoints.server.networking.payload.SWPayload;
import dev.phonis.sharedwaypoints.server.networking.protocol.persistant.Packets;
import dev.phonis.sharedwaypoints.server.networking.protocol.persistant.SWRegister;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class SWPlayHandler implements PlayPayloadHandler<SWPayload>
{

    public static final SWPlayHandler INSTANCE = new SWPlayHandler();

    @Override
    public void receive(SWPayload swPayload, ServerPlayNetworking.Context context)
    {
        ServerPlayer player = context.player();
        MinecraftServer server = context.server();
        PacketSender responseSender = context.responseSender();

        // can receive for the same player be called from multiple netty threads?
        // if so, theoretically packets could be lost if registering has not been completed by
        // the SWRegister receiver, synchronizing on player fixes this assuming SWRegister is the
        // first packet sent to receive....
        // this is probably not necessary but should not cause too much overhead
        synchronized (player.getUUID())
        {
            try
            {
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(swPayload.bytes()));

                if (!SWNetworkManager.INSTANCE.handleIfSubscribed(server, player.getUUID(), dis))
                {
                    byte packetID = dis.readByte();

                    if (Packets.Out.SWRegisterID == packetID)
                    {
                        SWRegister register = SWRegister.fromBytes(dis);

                        dis.close();
                        SWNetworkManager.INSTANCE.subscribePlayer(server, player, responseSender, register.protocolVersion);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
