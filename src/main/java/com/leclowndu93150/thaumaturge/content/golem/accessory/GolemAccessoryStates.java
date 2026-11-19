package com.leclowndu93150.thaumaturge.content.golem.accessory;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryStateView;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record GolemAccessoryStates(List<AccessoryStateSlot<?>> slots) implements GolemAccessoryStateView {
    public static final GolemAccessoryStates EMPTY = new GolemAccessoryStates(List.of());
    public static final StreamCodec<RegistryFriendlyByteBuf, GolemAccessoryStates> STREAM_CODEC =
            StreamCodec.of((buffer, value) -> RegistryContext.withRegistries(buffer.registryLookup(), () -> {
                encode(buffer, value);
                return null;
            }), buffer -> RegistryContext.withRegistries(buffer.registryLookup(), () -> decode(buffer)));
    public static final EntityDataSerializer<GolemAccessoryStates> ENTITY_DATA_SERIALIZER = EntityDataSerializer.simple(
            (buffer, states) -> STREAM_CODEC.encode(withRegistryLookup(buffer), states),
            buffer -> STREAM_CODEC.decode(withRegistryLookup(buffer)));

    public GolemAccessoryStates {
        slots = List.copyOf(slots);
    }

    @Override
    public <S> Optional<S> state(GolemAccessoryBehavior<S> behavior) {
        for (AccessoryStateSlot<?> slot : slots) {
            Optional<S> state = slot.stateFor(behavior);
            if (state.isPresent()) {
                return state;
            }
        }
        return Optional.empty();
    }

    public int encodedSize(RegistryAccess registries) {
        RegistryFriendlyByteBuf buf =
                new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            STREAM_CODEC.encode(buf, this);
            return buf.readableBytes();
        } finally {
            buf.release();
        }
    }

    private static RegistryFriendlyByteBuf withRegistryLookup(FriendlyByteBuf buffer) {
        return buffer instanceof RegistryFriendlyByteBuf contextual ? contextual
                : new RegistryFriendlyByteBuf(buffer, RegistryContext.resolve(null));
    }

    private static void encode(RegistryFriendlyByteBuf buf, GolemAccessoryStates states) {
        buf.writeVarInt(states.slots.size());
        for (AccessoryStateSlot<?> slot : states.slots) {
            VanillaStreamCodecs.RESOURCE_LOCATION.encode(buf, slot.accessory().id());
            slot.encode(buf);
        }
    }

    private static GolemAccessoryStates decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > buf.readableBytes()) {
            throw new DecoderException("Invalid golem accessory state count: " + count);
        }
        List<AccessoryStateSlot<?>> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = VanillaStreamCodecs.RESOURCE_LOCATION.decode(buf);
            GolemAccessory accessory = GolemAccessories.get(id);
            if (accessory == null || accessory.behavior().isEmpty()) {
                throw new DecoderException("Golem accessory " + id + " has no behaviour to decode its state");
            }
            slots.add(AccessoryStateSlot.decode(accessory, accessory.behavior().get(), buf));
        }
        return new GolemAccessoryStates(slots);
    }
}
