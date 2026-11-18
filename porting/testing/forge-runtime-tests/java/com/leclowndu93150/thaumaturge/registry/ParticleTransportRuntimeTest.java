package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TTColorParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ParticleTransportRuntimeTest {
    @BeforeAll
    static void registerParticles() throws Exception {
        var bus = BusBuilder.builder().build();
        TTParticles.register(bus);
        var forge = (ForgeRegistry<?>) ForgeRegistries.PARTICLE_TYPES;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.PARTICLE_TYPE;
        var constructor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        constructor.setAccessible(true);
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            bus.post(constructor.newInstance(ForgeRegistries.Keys.PARTICLE_TYPES, forge, vanilla));
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }

    @Test
    void everyDeferredParticleIsVisibleInTheNativeCommandAndPacketRegistry() {
        var registered = TTParticles.PARTICLES.getEntries();
        assertEquals(53, registered.size());
        assertEquals(44, registered.stream().filter(entry -> entry.get() instanceof TTParticleType<?>).count());
        for (var entry : registered) {
            var type = entry.get();
            assertSame(type, BuiltInRegistries.PARTICLE_TYPE.get(entry.getId()));
            assertSame(type, BuiltInRegistries.PARTICLE_TYPE.byId(BuiltInRegistries.PARTICLE_TYPE.getId(type)));
            assertFalse(type.getOverrideLimiter());
        }
    }

    @Test
    void nativeCommandConsumesTheCompoundAndLeavesCoordinatesAndSourceDefaults() throws Exception {
        var reader = new StringReader("thaumaturge:wispy_mote{color:6636321,age:32,gravity:0.125f} ~ ~1 ~");
        var settings = new WispyMoteParticleOptions(6636321, 32, 0.125F, -1, false);
        assertEquals(settings, ParticleArgument.readParticle(reader, BuiltInRegistries.PARTICLE_TYPE.asLookup()));
        assertEquals(" ~ ~1 ~", reader.getRemaining());
        assertEquals(settings, parse(settings.writeToString()));
        var malformed = assertThrows(CommandSyntaxException.class, () -> parse("thaumaturge:wispy_mote{color:1}"));
        assertTrue(malformed.getRawMessage().getString().contains("age"));
    }

    @Test
    void nativeColorCodecsRetainArgbBytesAndAlternativeRgbaSyntax() throws Exception {
        var options = assertInstanceOf(TTColorParticleOptions.class,
                parse("thaumaturge:focus_cloud{color:[0.25f,0.5f,1.0f,0.5f]}"));
        assertSame(TTParticles.FOCUS_CLOUD.get(), options.getType());
        assertEquals(0x7F3F7FFF, options.color());
        assertEquals(63 / 255.0F, options.getRed());
        assertEquals(127 / 255.0F, options.getGreen());
        assertEquals(1.0F, options.getBlue());
        assertEquals(127 / 255.0F, options.getAlpha());
        assertEquals(0xFF3F7FFF, TTParticles.colorOf(TTParticles.FOCUS_CLOUD, 0.25F, 0.5F, 1.0F).color());
        var tagged = new JsonObject();
        tagged.addProperty("type", "thaumaturge:focus_cloud");
        tagged.addProperty("color", 0x7F3F7FFF);
        assertEquals(tagged, Codecs.getOrThrow(ParticleTypes.CODEC.encodeStart(JsonOps.INSTANCE, options)));
        assertEquals(options, Codecs.getOrThrow(ParticleTypes.CODEC.parse(JsonOps.INSTANCE, tagged)));
        assertEquals(options, parse(options.writeToString()));
        assertThrows(CommandSyntaxException.class, () -> parse("thaumaturge:focus_cloud{color:[0.25f,0.5f,1.0f]}"));

        var bytes = new FriendlyByteBuf(Unpooled.buffer());
        try {
            options.writeToNetwork(bytes);
            assertEquals(Integer.BYTES, bytes.readableBytes());
            assertEquals(0x7F3F7FFF, bytes.readInt());
        } finally {
            bytes.release();
        }
    }

    @Test
    void nativeLevelParticlePacketPreservesBlockStateAndAllDebrisCoordinates() {
        var state = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        var debris = new BoreDebrisParticleOptions(state, 42, 1, 2, 3, -4, -5, -6);
        var packet = new ClientboundLevelParticlesPacket(debris, true, 6, 3, -2, 0.5F, 0.25F, 0.125F, 0.75F, 5);
        var bytes = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.write(bytes);
            var decoded = new ClientboundLevelParticlesPacket(bytes);
            assertEquals(debris, decoded.getParticle());
            assertEquals(packet.getX(), decoded.getX());
            assertEquals(packet.getCount(), decoded.getCount());
            assertTrue(decoded.isOverrideLimiter());
            assertFalse(bytes.isReadable());
        } finally {
            bytes.release();
        }
    }

    @Test
    void targetedPayloadPreservesNativeAndCustomOptionsAndCoordinates() {
        var colored = TTParticles.colorOf(TTParticles.BLOCK_MIST, 0x4080C0FF);
        assertEquals(colored, payloadRoundTrip(colored).options());
        assertSame(TTParticles.PRIMAL_FLARE.get(), payloadRoundTrip(TTParticles.PRIMAL_FLARE.get()).options());
        var vanilla = assertInstanceOf(DustParticleOptions.class, payloadRoundTrip(DustParticleOptions.REDSTONE).options());
        assertEquals(DustParticleOptions.REDSTONE.getColor(), vanilla.getColor());
        assertEquals(DustParticleOptions.REDSTONE.getScale(), vanilla.getScale());

        var stack = new ItemStack(Items.GOLD_INGOT, 3);
        stack.setHoverName(Component.literal("Infusion sample"));
        var crumbs = new InfusionCrumbsParticleOptions(stack, 4, -3, 2, 1, -2, 3);
        var decoded = assertInstanceOf(InfusionCrumbsParticleOptions.class, payloadRoundTrip(crumbs).options());
        assertTrue(ItemStack.matches(crumbs.stack(), decoded.stack()));
        assertEquals(crumbs.ty(), decoded.ty());
        assertEquals(crumbs.sz(), decoded.sz());
    }

    @Test
    void targetedPayloadRejectsAnUnknownParticleId() {
        var bytes = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            bytes.writeVarInt(Integer.MAX_VALUE);
            assertThrows(DecoderException.class, () -> ClientboundSpawnParticlePayload.STREAM_CODEC.decode(bytes));
        } finally {
            bytes.release();
        }
    }

    private static ParticleOptions parse(String command) throws CommandSyntaxException {
        return ParticleArgument.readParticle(new StringReader(command), BuiltInRegistries.PARTICLE_TYPE.asLookup());
    }

    private static ClientboundSpawnParticlePayload payloadRoundTrip(ParticleOptions options) {
        var payload = new ClientboundSpawnParticlePayload(options, 0.5, -5, 9, -0.125, 0.25, 0.375);
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var bytes = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            var codec = ClientboundSpawnParticlePayload.STREAM_CODEC;
            codec.encode(bytes, payload);
            var decoded = codec.decode(bytes);
            assertEquals(payload.x(), decoded.x());
            assertEquals(payload.y(), decoded.y());
            assertEquals(payload.z(), decoded.z());
            assertEquals(payload.vx(), decoded.vx());
            assertEquals(payload.vy(), decoded.vy());
            assertEquals(payload.vz(), decoded.vz());
            assertFalse(bytes.isReadable());
            return decoded;
        } finally {
            bytes.release();
        }
    }
}
