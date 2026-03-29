package dev.phonis.sharedwaypoints.server;

import dev.phonis.sharedwaypoints.server.commands.CommandWaypoint;
import dev.phonis.sharedwaypoints.server.commands.internal.SWCommandManager;
import dev.phonis.sharedwaypoints.server.interop.bluemap.BlueMapManager;
import dev.phonis.sharedwaypoints.server.interop.dynmap.DynmapManager;
import dev.phonis.sharedwaypoints.server.networking.SWNetworkManager;
import dev.phonis.sharedwaypoints.server.networking.SWPlayHandler;
import dev.phonis.sharedwaypoints.server.networking.payload.SWPayload;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class SharedWaypointsServer implements DedicatedServerModInitializer
{

    public static final int maxSupportedProtocolVersion = 1;
    public static final String configDirectory = "config/sharedwaypoints/";
    public static MinecraftServer minecraftServer;

    @Override
    public void onInitializeServer()
    {
        SWCommandManager.addCommand(new CommandWaypoint());
        SWCommandManager.register();
        PayloadTypeRegistry.playC2S().register(SWPayload.id, SWPayload.codec);
        PayloadTypeRegistry.playS2C().register(SWPayload.id, SWPayload.codec);
        ServerPlayNetworking.registerGlobalReceiver(SWPayload.id, SWPlayHandler.INSTANCE);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SWNetworkManager.INSTANCE.unsubscribePlayer(handler.player.getUUID()));
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> SharedWaypointsServer.minecraftServer = server);
        if (FabricLoader.getInstance().isModLoaded("dynmap"))
        {
            DynmapManager.INSTANCE.registerDynmap();
        }
        if (FabricLoader.getInstance().isModLoaded("bluemap"))
        {
            BlueMapManager.INSTANCE.registerBlueMap();
        }
    }

}
