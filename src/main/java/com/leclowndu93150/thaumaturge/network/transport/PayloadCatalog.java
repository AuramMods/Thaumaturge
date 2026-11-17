package com.leclowndu93150.thaumaturge.network.transport;

import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;

/** Maps stable payload IDs to their codecs, allowed direction and gameplay handler. */
public final class PayloadCatalog {
    public enum Flow { CLIENTBOUND, SERVERBOUND }

    private final Map<ResourceLocation, Entry<?>> entries = new ConcurrentHashMap<>();

    public <T extends ModPayload> void register(ModPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Flow flow, BiConsumer<T, PayloadContext> handler) {
        Entry<T> entry = new Entry<>(type, codec, flow, handler);
        if (entries.putIfAbsent(type.id(), entry) != null) {
            throw new IllegalStateException("Duplicate payload registration " + type.id());
        }
    }

    public void encode(RegistryFriendlyByteBuf buffer, ModPayload payload, Flow flow) {
        Entry<?> entry = entries.get(payload.type().id());
        if (entry == null || entry.flow() != flow) {
            throw new EncoderException("Payload " + payload.type().id() + " is not registered for " + flow);
        }
        buffer.writeResourceLocation(payload.type().id());
        entry.encode(buffer, payload);
    }

    public ModPayload decode(RegistryFriendlyByteBuf buffer, Flow flow) {
        ResourceLocation id = buffer.readResourceLocation();
        Entry<?> entry = entries.get(id);
        if (entry == null || entry.flow() != flow) {
            throw new DecoderException("Payload " + id + " is not registered for " + flow);
        }
        ModPayload payload = entry.codec().decode(buffer);
        if (!entry.type().equals(payload.type())) {
            throw new DecoderException("Payload codec for " + id + " returned " + payload.type().id());
        }
        if (buffer.isReadable()) {
            throw new DecoderException("Trailing bytes in payload " + id);
        }
        return payload;
    }

    public void handle(ModPayload payload, Flow flow, PayloadContext context) {
        Entry<?> entry = entries.get(payload.type().id());
        if (entry == null || entry.flow() != flow) {
            throw new DecoderException("Unexpected payload " + payload.type().id() + " for " + flow);
        }
        entry.handle(payload, context);
    }

    private record Entry<T extends ModPayload>(ModPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Flow flow, BiConsumer<T, PayloadContext> handler) {
        @SuppressWarnings("unchecked")
        void encode(RegistryFriendlyByteBuf buffer, ModPayload payload) {
            codec.encode(buffer, (T) payload);
        }

        @SuppressWarnings("unchecked")
        void handle(ModPayload payload, PayloadContext context) {
            handler.accept((T) payload, context);
        }
    }
}
