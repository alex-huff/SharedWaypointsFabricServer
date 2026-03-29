package dev.phonis.sharedwaypoints.server.networking.payload;

import dev.phonis.sharedwaypoints.server.networking.codec.SWPayloadCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SWPayload(byte[] bytes) implements CustomPacketPayload
{

    private static final Identifier sWIdentifier = Identifier.parse("sharedwaypoints:main");
    public static final Type<SWPayload> id = new Type<>(SWPayload.sWIdentifier);
    public static final SWPayloadCodec codec = new SWPayloadCodec();

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return SWPayload.id;
    }

}
