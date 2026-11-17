package com.leclowndu93150.thaumaturge.serialization.stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

/** Binary codec building blocks backed by the actual Minecraft 1.20.1 buffer APIs. */
public final class ByteBufCodecs {
    public static final int MAX_COLLECTION_SIZE = 1_048_576;
    public static final StreamCodec<ByteBuf, Boolean> BOOL =
            StreamCodec.of((buffer, value) -> buffer.writeBoolean(value), ByteBuf::readBoolean);
    public static final StreamCodec<ByteBuf, Byte> BYTE =
            StreamCodec.of((buffer, value) -> buffer.writeByte(value), ByteBuf::readByte);
    public static final StreamCodec<ByteBuf, Short> SHORT =
            StreamCodec.of((buffer, value) -> buffer.writeShort(value), ByteBuf::readShort);
    public static final StreamCodec<ByteBuf, Integer> INT =
            StreamCodec.of((buffer, value) -> buffer.writeInt(value), ByteBuf::readInt);
    public static final StreamCodec<ByteBuf, Float> FLOAT =
            StreamCodec.of((buffer, value) -> buffer.writeFloat(value), ByteBuf::readFloat);
    public static final StreamCodec<ByteBuf, Double> DOUBLE =
            StreamCodec.of((buffer, value) -> buffer.writeDouble(value), ByteBuf::readDouble);
    public static final StreamCodec<ByteBuf, Integer> VAR_INT = StreamCodec.of(
            (buffer, value) -> new FriendlyByteBuf(buffer).writeVarInt(value), buffer -> {
                int result = 0;
                for (int position = 0; position < 5; position++) {
                    byte next = buffer.readByte();
                    result |= (next & 0x7F) << (position * 7);
                    if ((next & 0x80) == 0) {
                        return result;
                    }
                }
                throw new DecoderException("VarInt exceeds five bytes");
            });
    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
    public static final StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = StreamCodec.of((buffer, value) -> {
        writeCount(buffer, value.length, MAX_COLLECTION_SIZE);
        buffer.writeBytes(value);
    }, buffer -> {
        int length = readCount(buffer, MAX_COLLECTION_SIZE);
        if (length > buffer.readableBytes()) {
            throw new DecoderException("Byte array exceeds remaining packet data");
        }
        byte[] result = new byte[length];
        buffer.readBytes(result);
        return result;
    });

    private ByteBufCodecs() {}

    public static StreamCodec<ByteBuf, String> stringUtf8(int maximumCharacters) {
        return StreamCodec.of((buffer, value) -> new FriendlyByteBuf(buffer).writeUtf(value, maximumCharacters),
                buffer -> new FriendlyByteBuf(buffer).readUtf(maximumCharacters));
    }

    public static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(StreamCodec<? super B, V> valueCodec) {
        return StreamCodec.of((buffer, value) -> {
            buffer.writeBoolean(value.isPresent());
            value.ifPresent(element -> valueCodec.encode(buffer, element));
        }, buffer -> buffer.readBoolean() ? Optional.of(valueCodec.decode(buffer)) : Optional.empty());
    }

    public static <B extends ByteBuf, V> Function<StreamCodec<B, V>, StreamCodec<B, List<V>>> list() {
        return list(MAX_COLLECTION_SIZE);
    }

    public static <B extends ByteBuf, V> Function<StreamCodec<B, V>, StreamCodec<B, List<V>>> list(int maximumSize) {
        return element -> StreamCodec.of((buffer, values) -> {
            writeCount(buffer, values.size(), maximumSize);
            for (V value : values) {
                element.encode(buffer, value);
            }
        }, buffer -> {
            int size = readCount(buffer, maximumSize);
            List<V> result = new ArrayList<>(size);
            for (int index = 0; index < size; index++) {
                result.add(element.decode(buffer));
            }
            return result;
        });
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
            IntFunction<M> factory, StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec) {
        return map(factory, keyCodec, valueCodec, MAX_COLLECTION_SIZE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
            IntFunction<M> factory, StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec,
            int maximumSize) {
        return StreamCodec.of((buffer, values) -> {
            writeCount(buffer, values.size(), maximumSize);
            for (Map.Entry<K, V> entry : values.entrySet()) {
                keyCodec.encode(buffer, entry.getKey());
                valueCodec.encode(buffer, entry.getValue());
            }
        }, buffer -> {
            int size = readCount(buffer, maximumSize);
            M result = factory.apply(size);
            for (int index = 0; index < size; index++) {
                K key = keyCodec.decode(buffer);
                result.put(key, valueCodec.decode(buffer));
            }
            return result;
        });
    }

    public static int readCount(ByteBuf buffer, int maximumSize) {
        int count = VAR_INT.decode(buffer);
        if (count < 0 || count > maximumSize) {
            throw new DecoderException("Collection size " + count + " outside 0.." + maximumSize);
        }
        return count;
    }

    public static void writeCount(ByteBuf buffer, int count, int maximumSize) {
        if (count < 0 || count > maximumSize) {
            throw new EncoderException("Collection size " + count + " outside 0.." + maximumSize);
        }
        VAR_INT.encode(buffer, count);
    }

    public static <V> StreamCodec<ByteBuf, V> idMapper(IntFunction<V> byId, ToIntFunction<V> toId) {
        return StreamCodec.of((buffer, value) -> {
            int id = toId.applyAsInt(value);
            if (id < 0) {
                throw new EncoderException("Unregistered value " + value);
            }
            VAR_INT.encode(buffer, id);
        }, buffer -> {
            int id = VAR_INT.decode(buffer);
            try {
                V value = byId.apply(id);
                if (value == null) {
                    throw new DecoderException("Unknown value id " + id);
                }
                return value;
            } catch (IndexOutOfBoundsException exception) {
                throw new DecoderException("Unknown value id " + id, exception);
            }
        });
    }

    public static <V> StreamCodec<RegistryFriendlyByteBuf, V> registry(ResourceKey<? extends Registry<V>> key) {
        return StreamCodec.of((buffer, value) -> {
            int id = buffer.registryAccess().registryOrThrow(key).getId(value);
            if (id < 0) {
                throw new EncoderException("Unregistered value in " + key.location() + ": " + value);
            }
            VAR_INT.encode(buffer, id);
        }, buffer -> {
            int id = VAR_INT.decode(buffer);
            return buffer.registryAccess().registryOrThrow(key).getHolder(id)
                    .orElseThrow(() -> new DecoderException("Unknown id " + id + " in " + key.location())).value();
        });
    }

    public static <V> StreamCodec<RegistryFriendlyByteBuf, Holder<V>> holderRegistry(
            ResourceKey<? extends Registry<V>> key) {
        return StreamCodec.of((buffer, holder) -> {
            Registry<V> registry = buffer.registryAccess().registryOrThrow(key);
            int id = registry.asHolderIdMap().getId(holder);
            if (id < 0) {
                throw new EncoderException("Unregistered holder in " + key.location());
            }
            VAR_INT.encode(buffer, id);
        }, buffer -> {
            int id = VAR_INT.decode(buffer);
            return buffer.registryAccess().registryOrThrow(key).getHolder(id)
                    .orElseThrow(() -> new DecoderException("Unknown holder id " + id + " in " + key.location()));
        });
    }

    public static <V> StreamCodec<ByteBuf, V> fromCodec(Codec<V> codec) {
        return StreamCodec.of((buffer, value) -> encodeNbt(buffer, NbtOps.INSTANCE, codec, value),
                buffer -> decodeNbt(buffer, NbtOps.INSTANCE, codec));
    }

    public static <V> StreamCodec<RegistryFriendlyByteBuf, V> fromCodecWithRegistries(Codec<V> codec) {
        return StreamCodec.of(
                (buffer, value) -> encodeNbt(buffer, RegistryOps.create(NbtOps.INSTANCE, buffer.registryAccess()), codec, value),
                buffer -> decodeNbt(buffer, RegistryOps.create(NbtOps.INSTANCE, buffer.registryAccess()), codec));
    }

    private static <V> void encodeNbt(ByteBuf buffer, DynamicOps<Tag> ops, Codec<V> codec, V value) {
        var encoded = codec.encodeStart(ops, value);
        Tag tag = encoded.result().orElseThrow(() -> new EncoderException(encoded.error().orElseThrow().message()));
        // The 1.20.1 network NBT API accepts compounds. A wrapper also carries scalar codecs.
        CompoundTag root = new CompoundTag();
        root.put("value", tag);
        new FriendlyByteBuf(buffer).writeNbt(root);
    }

    private static <V> V decodeNbt(ByteBuf buffer, DynamicOps<Tag> ops, Codec<V> codec) {
        CompoundTag root = new FriendlyByteBuf(buffer).readNbt();
        if (root == null || !root.contains("value")) {
            throw new DecoderException("Missing codec NBT value");
        }
        var decoded = codec.parse(ops, root.get("value"));
        return decoded.result().orElseThrow(() -> new DecoderException(decoded.error().orElseThrow().message()));
    }
}
