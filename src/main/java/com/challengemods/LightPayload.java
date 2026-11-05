package com.challengemods;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** phase: -1 = Ampel aus, 0 = gruen, 1 = orange, 2 = rot */
public record LightPayload(int phase) implements CustomPayload {
    public static final CustomPayload.Id<LightPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ChallengeMods.MOD_ID, "light"));
    public static final PacketCodec<RegistryByteBuf, LightPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.INTEGER, LightPayload::phase, LightPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
