package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitState;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintBroodTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedProfile;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.state.AttachmentKey;
import com.leclowndu93150.thaumaturge.state.AttachmentRegistry;
import com.leclowndu93150.thaumaturge.state.AttachmentStorage;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MobTraitRegistryRuntimeTest {
    private static RegistryObject<TaintBroodTrait> brood;
    private static RegistryObject<AddonChampion> addon;
    private static ResourceKey<MobTrait> broodKey;
    private static ResourceKey<MobTrait> addonKey;
    private static Registry<MobTrait> registry;

    @BeforeAll
    static void createForgeRegistryAndRegisterTypedTraitsFromTwoMods() throws Exception {
        var builtIns = DeferredRegister.create(MobTrait.REGISTRY_KEY, "thaumaturge");
        var registration = builtIns.makeRegistry(() -> new RegistryBuilder<MobTrait>().hasTags());
        brood = builtIns.register("taint_brood", TaintBroodTrait::new);
        var extensions = DeferredRegister.create(MobTrait.REGISTRY_KEY, "port_trait_addon");
        addon = extensions.register("taint_brood", AddonChampion::new);
        broodKey = ForgeRegistration.entryKey(MobTrait.REGISTRY_KEY, brood);
        addonKey = ForgeRegistration.entryKey(MobTrait.REGISTRY_KEY, addon);
        assertFalse(brood.isPresent());
        assertFalse(addon.isPresent());

        var bus = BusBuilder.builder().build();
        builtIns.register(bus);
        extensions.register(bus);
        var create = new NewRegistryEvent();
        bus.post(create);
        var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        fill.invoke(create);
        registry = ForgeRegistration.vanillaRegistry(registration);

        var forge = RegistryManager.ACTIVE.getRegistry(MobTrait.REGISTRY_KEY.location());
        var register = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        register.setAccessible(true);
        forge.unfreeze();
        try {
            bus.post(register.newInstance(MobTrait.REGISTRY_KEY, forge, registry));
        } finally {
            forge.freeze();
        }
    }

    @Test
    void keysMatchCanonicalHoldersForGameplayAndNamesWithoutLosingTheAddonNamespace() {
        var nativeBrood = registry.getHolderOrThrow(broodKey);
        var nativeAddon = registry.getHolderOrThrow(addonKey);
        assertSame(brood.getHolder().orElseThrow(), nativeBrood);
        assertSame(addon.getHolder().orElseThrow(), nativeAddon);
        assertSame(brood.get(), nativeBrood.value());
        assertSame(addon.get(), nativeAddon.value());
        assertNotEquals(broodKey, addonKey);
        assertEquals(broodKey.location().getPath(), addonKey.location().getPath());

        var carrier = mock(LivingEntity.class);
        doReturn(EntityType.SPIDER).when(carrier).getType();
        var bindings = mock(MobTraits.Bindings.class);
        MobTraits.bind(bindings);
        when(bindings.traits(carrier)).thenReturn(List.of(nativeBrood));
        assertTrue(MobTraits.has(carrier, broodKey));
        assertFalse(MobTraits.has(carrier, addonKey));
        assertTrue(MobTraits.isTainted(carrier));
        assertFalse(MobTraits.isChampion(carrier));
        when(bindings.traits(carrier)).thenReturn(List.of(nativeBrood, nativeAddon));
        assertSame(nativeAddon, MobTraits.champion(carrier).orElseThrow());
        assertEquals("mob_trait.port_trait_addon.taint_brood",
                ((TranslatableContents) MobTraitNames.of(nativeAddon).getContents()).getKey());
    }

    @Test
    void persistentAndNetworkStateResolveTheSameOrderedForgeHolders() {
        var nativeBrood = registry.getHolderOrThrow(broodKey);
        var nativeAddon = registry.getHolderOrThrow(addonKey);
        var state = MobTraitState.EMPTY.with(nativeAddon).with(nativeBrood);
        var server = RegistryLayer.createRegistryAccess().compositeAccess();
        var client = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        assertSame(registry, server.registryOrThrow(MobTrait.REGISTRY_KEY));
        assertSame(registry, client.registryOrThrow(MobTrait.REGISTRY_KEY));

        var schema = new AttachmentRegistry("thaumaturge");
        var key = schema.register("mob_traits", AttachmentKey.builder(() -> MobTraitState.EMPTY)
                .serialize(MobTraitState.CODEC.codec(), value -> !value.isEmpty())
                .sync(MobTraitState.STREAM_CODEC)
                .copyOnDeath());
        var first = new AttachmentStorage(schema, () -> server, () -> {});
        first.set(key, state);
        var saved = first.save();
        var ids = saved.getCompound("thaumaturge:mob_traits").getList("traits", Tag.TAG_STRING);
        assertEquals(2, ids.size());
        assertEquals("port_trait_addon:taint_brood", ids.getString(0));
        assertEquals("thaumaturge:taint_brood", ids.getString(1));
        var loaded = new AttachmentStorage(schema, () -> server, () -> {});
        loaded.load(saved);
        assertEquals(state, loaded.get(key));

        var buffer = Unpooled.buffer();
        try {
            MobTraitState.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buffer, server), loaded.get(key));
            var synced = MobTraitState.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buffer, client));
            assertFalse(buffer.isReadable());
            assertSame(nativeAddon, synced.traits().get(0));
            assertSame(nativeBrood, synced.traits().get(1));
            assertEquals(List.of(nativeAddon), synced.without(nativeBrood).traits());
        } finally {
            buffer.release();
        }
    }

    @Test
    void taintProfileDecodeResolvesTheDeferredTraitAndAddonKey() {
        var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryLayer.createRegistryAccess().compositeAccess());
        var json = JsonParser.parseString("""
                {"traits":["thaumaturge:taint_brood","port_trait_addon:taint_brood"],"natural_spawns":true}
                """);
        var profile = TaintedProfile.CODEC.parse(ops, json).result().orElseThrow();
        assertEquals(List.of(registry.getHolderOrThrow(broodKey), registry.getHolderOrThrow(addonKey)), profile.traits());
        assertTrue(profile.naturalSpawns());
        assertEquals(json, TaintedProfile.CODEC.encodeStart(ops, profile).result().orElseThrow());
    }

    private static final class AddonChampion implements MobTrait {
        @Override public boolean isChampion() { return true; }
    }
}
