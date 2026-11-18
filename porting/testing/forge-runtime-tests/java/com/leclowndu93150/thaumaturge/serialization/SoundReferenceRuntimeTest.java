package com.leclowndu93150.thaumaturge.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;

/** Native codecs/packets only: no client sound engine or audio device is opened. */
public class SoundReferenceRuntimeTest {
    @Test
    void registeredSoundValuesKeepNamedHolderIdentityAndVariableRange() {
        var id = new ResourceLocation("port_sound_registration", "named");
        var value = SoundEvent.createVariableRangeEvent(id);
        ForgeRegistries.SOUND_EVENTS.register(id, value);
        var holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(value);
        var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        var encoded = SoundEvent.CODEC.encodeStart(ops, holder).result().orElseThrow();
        assertEquals(id.toString(), encoded.getAsString());
        var decoded = SoundEvent.CODEC.parse(ops, encoded).result().orElseThrow();
        assertSame(holder, decoded);
        assertSame(value, decoded.value());
        assertEquals(16.0F, decoded.value().getRange(0.5F));
        assertEquals(24.0F, decoded.value().getRange(1.5F));
    }

    @Test
    void directFixedRangeSoundDataKeepsItsInlineSchema() {
        var id = new ResourceLocation("port_sound_registration", "direct");
        var holder = Holder.direct(SoundEvent.createFixedRangeEvent(id, 12.5F));
        var encoded = SoundEvent.CODEC.encodeStart(JsonOps.INSTANCE, holder).result().orElseThrow();
        assertEquals(id.toString(), encoded.getAsJsonObject().get("sound_id").getAsString());
        assertEquals(12.5F, encoded.getAsJsonObject().get("range").getAsFloat());
        var decoded = SoundEvent.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        assertEquals(id, decoded.value().getLocation());
        assertEquals(12.5F, decoded.value().getRange(0.5F));
        assertEquals(12.5F, decoded.value().getRange(4.0F));
    }

    @Test
    void actualVanillaSoundPacketsRetainNamedAndDirectValuesAndPlaybackParameters() {
        var named = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARMOR_EQUIP_LEATHER);
        var direct = Holder.direct(SoundEvent.createFixedRangeEvent(new ResourceLocation("port_sound_registration", "packet"), 19.0F));
        for (var sound : List.of(named, direct)) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                var source = new ClientboundSoundPacket(sound, SoundSource.PLAYERS, 0.25, 64.5, -2.125, 0.75F, 1.25F, 1234L);
                source.write(buffer);
                var decoded = new ClientboundSoundPacket(buffer);
                assertEquals(sound.value().getLocation(), decoded.getSound().value().getLocation());
                assertEquals(sound.value().getRange(2.0F), decoded.getSound().value().getRange(2.0F));
                assertEquals(SoundSource.PLAYERS, decoded.getSource());
                assertEquals(0.25, decoded.getX());
                assertEquals(64.5, decoded.getY());
                assertEquals(-2.125, decoded.getZ());
                assertEquals(0.75F, decoded.getVolume());
                assertEquals(1.25F, decoded.getPitch());
                assertEquals(1234L, decoded.getSeed());
                assertEquals(0, buffer.readableBytes());
                assertEquals(1, buffer.refCnt());
            } finally { buffer.release(); }
        }
    }
}
