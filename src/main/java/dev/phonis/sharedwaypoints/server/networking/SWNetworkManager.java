package dev.phonis.sharedwaypoints.server.networking;

import com.mojang.brigadier.context.CommandContext;
import dev.phonis.sharedwaypoints.server.SharedWaypointsServer;
import dev.phonis.sharedwaypoints.server.networking.payload.SWPayload;
import dev.phonis.sharedwaypoints.server.networking.protocol.action.SWAction;
import dev.phonis.sharedwaypoints.server.networking.protocol.action.SWWaypointInitializeAction;
import dev.phonis.sharedwaypoints.server.networking.protocol.persistant.SWPacket;
import dev.phonis.sharedwaypoints.server.networking.protocol.persistant.SWUnsupported;
import dev.phonis.sharedwaypoints.server.networking.protocol.v1.V1ProtocolAdapter;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SWNetworkManager
{

    public static final SWNetworkManager INSTANCE = new SWNetworkManager();

    private final Map<UUID, ProtocolAdapter> subscribedPlayers = new ConcurrentHashMap<>();

    private static byte[] packetToBytes(SWPacket packet) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream das = new DataOutputStream(baos);

        das.writeByte(packet.getID());
        packet.toBytes(das);
        das.close();
        return baos.toByteArray();
    }

    public void sendToSubscribed(SWAction action)
    {
        this.sendToSubscribed(SharedWaypointsServer.minecraftServer.getPlayerManager(), action);
    }

    // should be called from Server Thread
    public void sendToSubscribed(PlayerManager playerManager, SWAction action)
    {
        // although iteration over the hashmap does not guarantee that no modifications are made
        // during the iteration, the iterator should display that state of the map at the time
        // of its creation, making this safe
        for (Map.Entry<UUID, ProtocolAdapter> entry : this.subscribedPlayers.entrySet())
        {
            this.sendToPlayer(entry.getValue(), playerManager.getPlayer(entry.getKey()), action);
        }
    }

    // should be called from Server Thread
    public void sendIfSubscribed(ServerPlayerEntity player, SWAction action)
    {
        this.sendToPlayer(this.subscribedPlayers.get(player.getUuid()), player, action);
    }

    private void sendToPlayer(ProtocolAdapter adapter, @Nullable ServerPlayerEntity player, SWAction action)
    {
        if (player == null || adapter == null)
        {
            return;
        }

        this.sendPacketToPlayer(player, adapter.fromAction(action));
    }

    private void sendPacketToPlayer(ServerPlayerEntity player, SWPacket packet)
    {
        try
        {
            ServerPlayNetworking.send(player, new SWPayload(SWNetworkManager.packetToBytes(packet)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendPacketToSender(PacketSender sender, SWPacket packet)
    {
        try
        {
            sender.sendPacket(new SWPayload(SWNetworkManager.packetToBytes(packet)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // will be submitted to Server Thread
    public void handleAction(MinecraftServer server, UUID uuid, SWAction action)
    {

    }

    public boolean handleIfSubscribed(MinecraftServer server, UUID uuid, DataInputStream dis)
    {
        ProtocolAdapter adapter = this.subscribedPlayers.get(uuid);

        if (adapter != null)
        {
            try
            {
                SWAction action = adapter.toAction(dis);

                dis.close();
                server.execute(() -> this.handleAction(server, uuid, action));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    public void subscribePlayer(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
                                int protocolVersion)
    {
        ProtocolAdapter adapter = switch (protocolVersion)
        {
            case 1 -> V1ProtocolAdapter.INSTANCE;
            default -> null;
        };

        if (adapter != null)
        {
            UUID uuid = player.getUuid();

            this.subscribedPlayers.put(uuid, adapter);

            // theoretically this would allow a waypoint update or remove action to be sent to the client
            // before the waypoints are initialized. it is the client's job to fail gracefully in this
            // scenario.

            // if I were to instead subscribe the player after the waypoint update is queued, it would
            // fix the above issue but server-bound packets could be LOST since handleIfSubscribed would
            // return false and the packet would end up being discarded after trying to read a SWRegister packet
            // this race condition would be very rare, but certainly a more preferable outcome would be sending
            // and invalid waypoint action to the client.

            // it might make some sense to store some condition per player describing whether a waypoint initialize
            // packet has been sent yet. then the manager could discard the waypoint update if initialization has not
            // happened yet.
            server.execute(() ->
            {
                // lexically refer to MinecraftServer singleton instead of ServerPlayerEntity object
                // because I don't know if ServerPlayerEntity will be valid when this is run.

                // I run this on the main thread since I don't want to have to worry about cross-thread
                // synchronization of waypoint state when I can just construct the waypoint initialization action
                // on the main thread, where all the other waypoint actions are processed.

                // if I were to construct the waypoint initialize action here on the netty thread, then I would
                // not need to worry about the issues above.
                // this would obviously require significant synchronization of the waypoint state for it to
                // be safe
                this.sendToPlayer(adapter, server.getPlayerManager().getPlayer(uuid), new SWWaypointInitializeAction());
            });

            // TLDR; currently it is feasible for the client to be told to remove or update a waypoint BEFORE
            // waypoints have been sent to the client. It is the client's responsibility to fail gracefully.
        }
        else
        {
            this.sendPacketToSender(responseSender, new SWUnsupported(SharedWaypointsServer.maxSupportedProtocolVersion));
        }
    }

    public void unsubscribePlayer(UUID uuid)
    {
        this.subscribedPlayers.remove(uuid);
    }

}
