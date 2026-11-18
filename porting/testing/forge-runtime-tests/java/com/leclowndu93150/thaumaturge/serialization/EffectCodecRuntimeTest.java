package com.leclowndu93150.thaumaturge.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

public class EffectCodecRuntimeTest {
    @Test
    void nativeVanillaEffectConstantsRetainTheirActualHoldersAndNamedCodecIds() throws ReflectiveOperationException {
        int checked = 0;
        for (var field : MobEffects.class.getFields()) {
            if (field.getType() != MobEffect.class) continue;
            var effect = (MobEffect) field.get(null);
            var holder = EffectCodecs.holder(effect);
            var forgeHolder = ForgeRegistries.MOB_EFFECTS.getDelegateOrThrow(effect);
            assertEquals(forgeHolder.unwrapKey(), holder.unwrapKey());
            assertSame(forgeHolder.value(), holder.value());
            assertTrue(holder.isBound());
            var encoded = EffectCodecs.MOB_EFFECT.encodeStart(JsonOps.INSTANCE, holder).result().orElseThrow();
            assertEquals(BuiltInRegistries.MOB_EFFECT.getKey(effect).toString(), encoded.getAsString());
            assertSame(holder, EffectCodecs.MOB_EFFECT.parse(JsonOps.INSTANCE, encoded).result().orElseThrow());
            checked++;
        }
        assertEquals(BuiltInRegistries.MOB_EFFECT.keySet().stream()
                .filter(id -> id.getNamespace().equals("minecraft")).count(), checked);
    }

    private MobEffectInstance parse(String json) {
        return EffectCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();
    }

    private MobEffectInstance nested() {
        return EffectCodecs.fromDetails(MobEffects.MOVEMENT_SPEED,
                new EffectDetails(255, -1, true, false, true,
                        Optional.of(new EffectDetails(128, 60, false, true, false, Optional.empty()))));
    }

    @Test
    void namedEffectSchemaPreservesNativeStateAndRecursiveHiddenEffectsInJsonAndNbt() {
        var source = nested();
        var json = EffectCodecs.CODEC.encodeStart(JsonOps.INSTANCE, source).result().orElseThrow();
        assertEquals("minecraft:speed", json.getAsJsonObject().get("id").getAsString());
        assertEquals(-1, json.getAsJsonObject().get("amplifier").getAsInt());
        assertEquals(EffectCodecs.details(source), EffectCodecs.details(parse(json.toString())));
        var nbt = EffectCodecs.CODEC.encodeStart(NbtOps.INSTANCE, source).result().orElseThrow();
        var decoded = EffectCodecs.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow();
        assertSame(MobEffects.MOVEMENT_SPEED, decoded.getEffect());
        assertEquals(EffectCodecs.details(source), EffectCodecs.details(decoded));
        assertTrue(decoded.isInfiniteDuration());
    }

    @Test
    void actualUnsignedByteCodecAndDependentIconDefaultsMatchTheSourceGame() {
        var value = parse("{\"id\":\"minecraft:speed\",\"show_particles\":false,\"amplifier\":-1}");
        assertEquals(255, value.getAmplifier());
        assertFalse(value.isVisible());
        assertFalse(value.showIcon());
        assertEquals(0, value.getDuration());
        assertEquals(0, parse("{\"id\":\"minecraft:speed\",\"amplifier\":256}").getAmplifier());
        for (String input : List.of("{}", "{\"id\":\"thaumaturge:missing\"}",
                "{\"id\":\"minecraft:speed\",\"amplifier\":\"bad\"}",
                "{\"id\":\"minecraft:speed\",\"show_icon\":\"bad\"}",
                "{\"id\":\"minecraft:speed\",\"hidden_effect\":{\"duration\":\"bad\"}}")) {
            var result = EffectCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(input));
            assertTrue(result.error().isPresent(), input);
            assertTrue(result.result().isEmpty(), input);
        }
    }

    @Test
    void sourceConstructorsKeepHolderFlagsClampAndCopyBehavior() {
        var holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.MOVEMENT_SPEED);
        assertEquals(0, EffectCodecs.instance(holder).getDuration());
        assertEquals(0, EffectCodecs.instance(holder, 10, -1).getAmplifier());
        assertEquals(255, EffectCodecs.instance(holder, 10, 300).getAmplifier());
        var flags = EffectCodecs.instance(holder, 50, 2, true, false);
        assertTrue(flags.isAmbient());
        assertFalse(flags.isVisible());
        assertFalse(flags.showIcon());
        var source = nested();
        var copy = EffectCodecs.instance(source);
        assertEquals(source.getAmplifier(), copy.getAmplifier());
        assertEquals(source.getDuration(), copy.getDuration());
        assertEquals(source.showIcon(), copy.showIcon());
        assertNull(((EffectInstanceAccess) copy).thaumaturge$hiddenEffect());
    }

    @Test
    void nativeEntityNbtPreservesTheEntireValidSourceAmplifierRange() {
        for (int amplifier : List.of(0, 127, 128, 255)) {
            var source = EffectCodecs.instance(MobEffects.MOVEMENT_SPEED, 200, amplifier);
            var saved = source.save(new CompoundTag());
            assertEquals(amplifier, MobEffectInstance.load(saved).getAmplifier(), "amplifier " + amplifier);
            assertEquals(amplifier > 127 ? Tag.TAG_INT : Tag.TAG_BYTE, saved.getTagType("Amplifier"));
        }
        var source = nested();
        var decoded = MobEffectInstance.load(source.save(new CompoundTag()));
        assertEquals(EffectCodecs.details(source), EffectCodecs.details(decoded));
    }

    @Test
    void legacySignedByteNbtKeepsNativeBehaviorWhileExtendedIntsRetainTheirValue() {
        var saved = EffectCodecs.instance(MobEffects.MOVEMENT_SPEED, 20).save(new CompoundTag());
        saved.putByte("Amplifier", (byte) -1);
        assertEquals(0, MobEffectInstance.load(saved).getAmplifier());
        saved.putInt("Amplifier", 255);
        assertEquals(255, MobEffectInstance.load(saved).getAmplifier());
    }

    @Test
    void originalCodecWireBridgeRetainsEffectsAndBufferOwnership() {
        var stream = ByteBufCodecs.fromCodecWithRegistries(EffectCodecs.CODEC);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        try {
            var source = nested();
            stream.encode(buffer, source);
            assertEquals(EffectCodecs.details(source), EffectCodecs.details(stream.decode(buffer)));
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void actualForgeRegisteredEffectsKeepTheirNamesAndNativeSaveIds() {
        var effect = new MobEffect(MobEffectCategory.NEUTRAL, 0x112233) {};
        var name = new ResourceLocation("thaumaturge", "port_effect_fixture");
        ForgeRegistries.MOB_EFFECTS.register(name, effect);
        var source = EffectCodecs.instance(effect, 100, 255);
        var encoded = EffectCodecs.CODEC.encodeStart(JsonOps.INSTANCE, source).result().orElseThrow();
        assertEquals(name.toString(), encoded.getAsJsonObject().get("id").getAsString());
        assertSame(effect, parse(encoded.toString()).getEffect());
        assertSame(effect, MobEffectInstance.load(source.save(new CompoundTag())).getEffect());
    }

    @Test
    void differentHiddenEffectTypesFailEncodingInsteadOfBeingSilentlyChanged() {
        var source = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, true, true,
                EffectCodecs.instance(MobEffects.POISON, 40), Optional.empty());
        var result = EffectCodecs.CODEC.encodeStart(JsonOps.INSTANCE, source);
        assertTrue(result.error().orElseThrow().message().contains("parent effect type"));
        assertTrue(result.result().isEmpty());
    }
}
