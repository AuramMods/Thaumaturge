package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public record ClientboundSpawnParticlePayload(
        ParticleOptions options, double x, double y, double z, double vx, double vy, double vz)
        implements ModPayload {
    public static final Type<ClientboundSpawnParticlePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "spawn_particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSpawnParticlePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeId(BuiltInRegistries.PARTICLE_TYPE, p.options().getType());
                        p.options().writeToNetwork(buf);
                        buf.writeDouble(p.x());
                        buf.writeDouble(p.y());
                        buf.writeDouble(p.z());
                        buf.writeDouble(p.vx());
                        buf.writeDouble(p.vy());
                        buf.writeDouble(p.vz());
                    },
                    buf -> new ClientboundSpawnParticlePayload(
                            readOptions(buf.readById(BuiltInRegistries.PARTICLE_TYPE), buf),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble()));

    private static <T extends ParticleOptions> T readOptions(ParticleType<T> type, RegistryFriendlyByteBuf buffer) {
        if (type == null) {
            throw new DecoderException("Unknown particle type");
        }
        return type.getDeserializer().fromNetwork(type, buffer);
    }

    public ClientboundSpawnParticlePayload(ParticleOptions options, double x, double y, double z) {
        this(options, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
