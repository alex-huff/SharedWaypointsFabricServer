package dev.phonis.sharedwaypoints.server.networking.codec;

import dev.phonis.sharedwaypoints.server.networking.payload.SWPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SWPayloadCodec implements StreamCodec<RegistryFriendlyByteBuf, SWPayload>
{

    @Override
    public SWPayload decode(RegistryFriendlyByteBuf buf)
    {
        return new SWPayload(buf.readByteArray());
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, SWPayload value)
    {
        buf.writeByteArray(value.bytes());
    }

}
