package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.Test;

public class EffectRegistrationRuntimeTest {
    @Test
    void commandStyleSupplierTablesStayLazyAndResolveRealNativeEffectsAfterRegistration() throws Exception {
        var registration = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "port_effect_registration");
        RegistryObject<MobEffect> handle = registration.register("lazy", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x123456) {});
        Map<String, Supplier<MobEffect>> commands = Map.of("lazy", handle);
        assertFalse(handle.isPresent());
        assertSame(handle, commands.get("lazy"));
        var bus = BusBuilder.builder().build();
        registration.register(bus);
        var registry = (ForgeRegistry<?>) ForgeRegistries.MOB_EFFECTS;
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        boolean locked = registry.isLocked();
        registry.unfreeze();
        try { bus.post(ctor.newInstance(ForgeRegistries.Keys.MOB_EFFECTS, registry, BuiltInRegistries.MOB_EFFECT)); }
        finally { if (locked) registry.freeze(); else registry.unfreeze(); }
        assertTrue(handle.isPresent());
        var value = commands.get("lazy").get();
        assertSame(handle.get(), value);
        assertSame(value, ForgeRegistries.MOB_EFFECTS.getValue(handle.getId()));
        var holder = EffectCodecs.holder(value);
        assertEquals(handle.getId(), holder.unwrapKey().orElseThrow().location());
        var effect = EffectCodecs.instance(value, 200, 128, true, false, true);
        var encoded = EffectCodecs.CODEC.encodeStart(JsonOps.INSTANCE, effect).result().orElseThrow();
        assertEquals(handle.getId().toString(), encoded.getAsJsonObject().get("id").getAsString());
        var decoded = EffectCodecs.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        assertSame(value, decoded.getEffect());
        assertEquals(128, decoded.getAmplifier());
        assertEquals(EffectCodecs.details(effect), EffectCodecs.details(decoded));
    }
}
