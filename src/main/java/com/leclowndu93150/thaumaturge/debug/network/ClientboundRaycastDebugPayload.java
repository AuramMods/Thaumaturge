package com.leclowndu93150.thaumaturge.debug.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.HitResult;

public record ClientboundRaycastDebugPayload(HitResult result) implements ModPayload {

    public static final Type<ClientboundRaycastDebugPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "debug/raycast_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRaycastDebugPayload> STREAM_CODEC =
            StreamCodec.composite(
                    HitResultStreamCodecs.HIT_RESULT,
                    ClientboundRaycastDebugPayload::result,
                    ClientboundRaycastDebugPayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
