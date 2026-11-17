package com.leclowndu93150.thaumaturge.serialization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Runs the actual transformed packet handler with controlled client/world dependencies. */
public class EffectPacketClientRuntimeTest {
    @Test
    void nativeEffectPacketsReconstructUnsignedAmplifiersThroughTheRealClientHandler() throws ReflectiveOperationException {
        var minecraft = mock(Minecraft.class);
        when(minecraft.isSameThread()).thenReturn(true);
        var level = mock(ClientLevel.class);
        var target = mock(LivingEntity.class);
        when(level.getEntity(42)).thenReturn(target);
        var listener = mock(ClientPacketListener.class, CALLS_REAL_METHODS);
        for (var entry : java.util.Map.of("minecraft", minecraft, "level", level).entrySet()) {
            var field = ClientPacketListener.class.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(listener, entry.getValue());
        }
        for (int amplifier : List.of(0, 127, 128, 255)) {
            clearInvocations(target);
            var effect = EffectCodecs.instance(MobEffects.MOVEMENT_SPEED, 200, amplifier, true, false, true);
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                new ClientboundUpdateMobEffectPacket(42, effect).write(buffer);
                var decoded = new ClientboundUpdateMobEffectPacket(buffer);
                assertEquals(amplifier, Byte.toUnsignedInt(decoded.getEffectAmplifier()));
                assertEquals(0, buffer.readableBytes());
                listener.handleUpdateMobEffect(decoded);
                var applied = ArgumentCaptor.forClass(MobEffectInstance.class);
                verify(target).forceAddEffect(applied.capture(), isNull());
                assertEquals(amplifier, applied.getValue().getAmplifier(), "amplifier " + amplifier);
                assertEquals(200, applied.getValue().getDuration());
                assertTrue(applied.getValue().isAmbient());
                assertFalse(applied.getValue().isVisible());
                assertTrue(applied.getValue().showIcon());
            } finally { buffer.release(); }
        }
    }
}
