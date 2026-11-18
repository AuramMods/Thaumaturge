package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class TTParticleType<T extends TTParticleOptions> extends ParticleType<T> {
    private static final DynamicCommandExceptionType INVALID_OPTIONS =
            new DynamicCommandExceptionType(message ->
                    Component.translatableWithFallback("particle.invalidOptions", "Invalid particle options: %s", message));

    private final Codec<T> codec;
    private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

    public TTParticleType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this(type -> codec, type -> streamCodec);
    }

    protected TTParticleType(Function<TTParticleType<T>, MapCodec<T>> codec,
            Function<TTParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec) {
        super(false, deserializer());
        this.codec = codec.apply(this).codec();
        this.streamCodec = streamCodec.apply(this);
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @SuppressWarnings("unchecked")
    void write(FriendlyByteBuf source, TTParticleOptions options) {
        streamCodec.encode(contextual(source), (T) options);
    }

    @SuppressWarnings("unchecked")
    String commandString(TTParticleOptions options) {
        var tag = Codecs.getOrThrow(codec.encodeStart(RegistryContext.ops(), (T) options));
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this).toString() + tag;
    }

    private static RegistryFriendlyByteBuf contextual(FriendlyByteBuf source) {
        return source instanceof RegistryFriendlyByteBuf buffer ? buffer
                : new RegistryFriendlyByteBuf(source, RegistryContext.resolve(null));
    }

    private static <T extends TTParticleOptions> ParticleOptions.Deserializer<T> deserializer() {
        return new ParticleOptions.Deserializer<>() {
            @Override
            public T fromCommand(ParticleType<T> type, StringReader reader) throws CommandSyntaxException {
                var tag = reader.canRead() && reader.peek() == '{'
                        ? new TagParser(reader).readStruct() : new CompoundTag();
                var decoded = type.codec().parse(RegistryContext.ops(), tag);
                return decoded.result().orElseThrow(() ->
                        INVALID_OPTIONS.createWithContext(reader, decoded.error().orElseThrow().message()));
            }

            @Override
            public T fromNetwork(ParticleType<T> type, FriendlyByteBuf source) {
                return ((TTParticleType<T>) type).streamCodec.decode(contextual(source));
            }
        };
    }
}
