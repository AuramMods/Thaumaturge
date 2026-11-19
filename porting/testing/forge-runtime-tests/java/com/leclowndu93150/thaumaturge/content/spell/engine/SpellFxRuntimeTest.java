package com.leclowndu93150.thaumaturge.content.spell.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.spell.SpellRegistries;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import com.leclowndu93150.thaumaturge.content.spell.carrier.CarrierPayload;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SpellLook;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpellFxPayload;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import com.leclowndu93150.thaumaturge.registry.TTSpellFx;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SpellFxRuntimeTest {
    private static final ResourceLocation UNKNOWN = ResourceLocation.fromNamespaceAndPath("port_spell_test", "missing");
    private static RegistryObject<SpellFxStyle> mist;

    @BeforeAll
    static void createForgeStyleRegistryAfterBinding() throws Exception {
        var bus = BusBuilder.builder().build();
        var addon = DeferredRegister.create(SpellFxStyle.REGISTRY_KEY, "port_spell_test");
        mist = addon.register("mist", () -> (level, at, motion, color, random) ->
                level.addParticle(ParticleTypes.CLOUD, at.x, at.y, at.z, motion.x, motion.y, motion.z));
        TTSpellFx.register(bus);
        addon.register(bus);

        // Bind during construction, when the Forge wrappers have yet to be created.
        var behaviors = new MappedRegistry<>(SpellBehaviorType.REGISTRY_KEY, Lifecycle.stable());
        var actions = new MappedRegistry<>(SpellActionType.REGISTRY_KEY, Lifecycle.stable());
        SpellRegistries.bind(() -> behaviors, () -> actions, TTSpellFx::registry);
        var create = new NewRegistryEvent();
        bus.post(create);
        var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        fill.invoke(create);

        var forge = RegistryManager.ACTIVE.getRegistry(SpellFxStyle.REGISTRY_KEY.location());
        var register = RegisterEvent.class.getDeclaredConstructor(
                net.minecraft.resources.ResourceKey.class, ForgeRegistry.class, Registry.class);
        register.setAccessible(true);
        forge.unfreeze();
        try {
            bus.post(register.newInstance(SpellFxStyle.REGISTRY_KEY, forge, TTSpellFx.registry()));
        } finally {
            forge.freeze();
        }
    }

    @Test
    void batchKeepsRegisteredStylesAndFallsBackToTheNativeSparkleHolder() {
        var level = mock(ServerLevel.class);
        var batch = new SpellFxBatch(level);
        var anchor = new Vec3(13.0, 72.5, -25.0);
        var offset = new Vec3(1.25, 2.0, -4.5);
        var heading = new Vec3(0.5, -0.25, 1.0);
        var sent = ArgumentCaptor.forClass(ClientboundSpellFxPayload.class);
        try (var transport = mockStatic(PayloadNetwork.class)) {
            batch.impact(mist.getId(), anchor, 0x123456);
            batch.burst(UNKNOWN, anchor.add(offset), heading, 0xFEDCBA);
            batch.flush();
            batch.flush();
            transport.verify(() -> PayloadNetwork.sendToPlayersNear(
                    eq(level), isNull(), eq(anchor.x), eq(anchor.y), eq(anchor.z), eq(64.0), sent.capture()));
            transport.verifyNoMoreInteractions();
        }
        assertEquals(anchor, sent.getValue().anchor());
        assertEquals(List.of(
                new ClientboundSpellFxPayload.Event(
                        mist.getHolder().orElseThrow(), 0, 0, 0, 0, 0, 0, 0x123456, false),
                new ClientboundSpellFxPayload.Event(
                        TTSpellFx.SPARKLE.getHolder().orElseThrow(), 1.25F, 2.0F, -4.5F,
                        0.5F, -0.25F, 1.0F, 0xFEDCBA, true)),
                sent.getValue().events());
    }

    @Test
    void encodedStylesResolveToCanonicalForgeHolders() {
        RegistryAccess server = RegistryLayer.createRegistryAccess().compositeAccess();
        RegistryAccess receiver = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        assertSame(TTSpellFx.registry(), SpellRegistries.fxStyles());
        assertSame(TTSpellFx.registry(), server.registryOrThrow(SpellFxStyle.REGISTRY_KEY));
        assertSame(TTSpellFx.registry(), receiver.registryOrThrow(SpellFxStyle.REGISTRY_KEY));
        var nativeMist = mist.getHolder().orElseThrow();
        var nativeSparkle = TTSpellFx.SPARKLE.getHolder().orElseThrow();
        var packet = new ClientboundSpellFxPayload(new Vec3(20.5, 60, 30), List.of(
                new ClientboundSpellFxPayload.Event(nativeMist, 1.25F, 0.5F, 2, -2, 0, 1, 0x445566, true),
                new ClientboundSpellFxPayload.Event(nativeSparkle, 0, -1, 0, 0, 0, 0, 0x778899, false)));
        var data = Unpooled.buffer();
        try {
            ClientboundSpellFxPayload.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(data, server), packet);
            var decoded = ClientboundSpellFxPayload.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(data, receiver));
            assertEquals(packet, decoded);
            assertEquals(0, data.readableBytes());
            assertSame(nativeMist, decoded.events().get(0).style());
            assertSame(nativeSparkle, decoded.events().get(1).style());
        } finally {
            data.release();
        }
    }

    @Test
    void carrierUsesItsRegisteredStyleAndSkipsAnUnknownStyle() {
        Level level = mock(Level.class);
        when(level.getRandom()).thenReturn(RandomSource.create(12));
        var at = new Vec3(14, 70, -23);
        var motion = new Vec3(0.2, 0.3, 0.4);
        CarrierPayload.particle(level, new SpellLook(mist.getId(), 0x123456), at, motion);
        CarrierPayload.particle(level, new SpellLook(UNKNOWN, 0xFFFFFF), at, motion);
        verify(level).getRandom();
        verify(level).addParticle(ParticleTypes.CLOUD, at.x, at.y, at.z, motion.x, motion.y, motion.z);
        verifyNoMoreInteractions(level);
    }
}
