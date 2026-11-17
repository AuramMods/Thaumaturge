package com.leclowndu93150.thaumaturge.serialization.stream;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class RegistryCodecTest {
    static {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    private static final ResourceKey<Registry<String>> KEY =
            ResourceKey.createRegistryKey(new ResourceLocation("thaumaturge", "codec_test"));

    @Test
    void registryValuesAndHoldersUseTheConnectionsRegistryIds() {
        MappedRegistry<String> registry = registry();
        var access = new RegistryAccess.ImmutableRegistryAccess(List.of(registry));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
        try {
            var values = ByteBufCodecs.registry(KEY);
            var holders = ByteBufCodecs.holderRegistry(KEY);
            values.encode(buffer, "air");
            assertEquals(0, buffer.readVarInt());
            Holder<String> holder = registry.getHolderOrThrow(ResourceKey.create(KEY, new ResourceLocation("thaumaturge", "earth")));
            holders.encode(buffer, holder);
            assertSame(holder, holders.decode(buffer));
            values.encode(buffer, "earth");
            assertEquals("earth", values.decode(buffer));
            assertThrows(EncoderException.class, () -> values.encode(buffer, "unregistered"));
            buffer.writeVarInt(999);
            assertThrows(DecoderException.class, () -> values.decode(buffer));
            buffer.writeVarInt(-1);
            assertThrows(DecoderException.class, () -> holders.decode(buffer));
        } finally { buffer.release(); }
    }

    @Test
    void registryAwareNbtResolvesReferences() {
        MappedRegistry<String> registry = registry();
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), new RegistryAccess.ImmutableRegistryAccess(List.of(registry)));
        try {
            var codec = ByteBufCodecs.fromCodecWithRegistries(RegistryFixedCodec.create(KEY));
            Holder<String> holder = registry.getHolderOrThrow(ResourceKey.create(KEY, new ResourceLocation("thaumaturge", "earth")));
            codec.encode(buffer, holder);
            assertSame(holder, codec.decode(buffer));
        } finally { buffer.release(); }
    }

    @Test
    void vanillaIdentifiersCoordinatesDirectionsAndUuidsRoundTrip() {
        assertEquals(new ResourceLocation("thaumaturge", "aura"), StreamCodecTest.roundTrip(
                VanillaStreamCodecs.RESOURCE_LOCATION, new ResourceLocation("thaumaturge", "aura")));
        assertEquals(new BlockPos(-30000000, -64, 29999999), StreamCodecTest.roundTrip(
                VanillaStreamCodecs.BLOCK_POS, new BlockPos(-30000000, -64, 29999999)));
        UUID uuid = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");
        assertEquals(uuid, StreamCodecTest.roundTrip(VanillaStreamCodecs.UUID, uuid));
        for (Direction direction : Direction.values()) {
            assertEquals(direction, StreamCodecTest.roundTrip(VanillaStreamCodecs.DIRECTION, direction));
        }
        var key = ResourceKey.create(KEY, new ResourceLocation("thaumaturge", "air"));
        assertEquals(key, StreamCodecTest.roundTrip(VanillaStreamCodecs.resourceKey(KEY), key));
    }

    private static MappedRegistry<String> registry() {
        var registry = new MappedRegistry<String>(KEY, Lifecycle.stable());
        Registry.register(registry, new ResourceLocation("thaumaturge", "air"), "air");
        Registry.register(registry, new ResourceLocation("thaumaturge", "earth"), "earth");
        registry.freeze();
        return registry;
    }
}
