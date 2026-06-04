package com.wiredtext;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WiredTextSyncPayload(boolean active, boolean overdrive, long minDelayMs, long maxDelayMs)
        implements CustomPayload {

    public static final Id<WiredTextSyncPayload> ID =
            new Id<>(Identifier.of("wiredtext", "sync"));

    public static final PacketCodec<PacketByteBuf, WiredTextSyncPayload> CODEC =
            PacketCodec.of(
                (value, buf) -> {
                    buf.writeBoolean(value.active());
                    buf.writeBoolean(value.overdrive());
                    buf.writeLong(value.minDelayMs());
                    buf.writeLong(value.maxDelayMs());
                },
                buf -> new WiredTextSyncPayload(
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readLong(),
                    buf.readLong()
                )
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
