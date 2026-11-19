package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonPrimitive;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyRegistryAliases;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Set;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.Test;

class LegacyRegistryAliasRuntimeTest {
    private static ResourceLocation oldId(String path) {
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", path);
    }

    @Test
    void forgeRegistrationPreservesAliasesInSnapshotsAndResolvesSavedStacksAndHolders() throws Exception {
        var forge = (ForgeRegistry<Item>) ForgeRegistries.ITEMS;
        Registry<Item> view = BuiltInRegistries.ITEM;
        var bus = BusBuilder.builder().build();
        // Priority must keep alias discovery after the registry declarations even when subscribed first.
        LegacyRegistryAliases.register(bus);
        var currentItems = DeferredRegister.create(ForgeRegistries.ITEMS, TTIds.MODID);
        var bore = currentItems.register("arcane_bore", () -> new Item(new Item.Properties()));
        var currentConflict = currentItems.register("port_alias_collision", () -> new Item(new Item.Properties()));
        var currentMissing = currentItems.register("port_alias_dangling", () -> new Item(new Item.Properties()));
        currentItems.register("port_alias_existing", () -> new Item(new Item.Properties()));
        currentItems.register(bus);
        var oldItems = DeferredRegister.create(ForgeRegistries.ITEMS, "thaumcraft");
        var oldConflict = oldItems.register("port_alias_collision", () -> new Item(new Item.Properties()));
        oldItems.register(bus);

        var constructor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        constructor.setAccessible(true);
        boolean wasLocked = forge.isLocked();
        var writable = (MappedRegistry<Item>) view;
        writable.unfreeze();
        forge.unfreeze();
        try {
            forge.addAlias(oldId("port_alias_existing"), view.getKey(Items.PAPER));
            forge.addAlias(oldId("port_alias_dangling"), oldId("port_alias_unregistered"));
            bus.post(constructor.newInstance(Registries.ITEM, forge, view));
        } finally {
            if (wasLocked) forge.freeze();
            writable.freeze();
        }

        var canonicalKey = ResourceKey.create(Registries.ITEM, bore.getId());
        var canonicalHolder = view.getHolderOrThrow(canonicalKey);
        var snapshot = ForgeRegistry.Snapshot.read(forge.makeSnapshot().write());
        for (var legacy : List.of(oldId("arcane_bore"), TTIds.rl("turret_bore"))) {
            var oldKey = ResourceKey.create(Registries.ITEM, legacy);
            assertEquals(bore.getId(), snapshot.aliases.get(legacy));
            assertSame(bore.get(), forge.getValue(legacy));
            assertSame(bore.get(), view.get(oldKey));
            assertSame(canonicalHolder, view.getHolderOrThrow(oldKey));
            assertSame(canonicalHolder, view.asLookup().getOrThrow(oldKey));
            assertSame(canonicalHolder, forge.getHolder(legacy).orElseThrow());
            assertFalse(view.keySet().contains(legacy));

            CompoundTag stackTag = new CompoundTag();
            stackTag.putString("id", legacy.toString());
            stackTag.putByte("Count", (byte) 2);
            ItemStack stack = ItemStack.of(stackTag);
            assertSame(bore.get(), stack.getItem());
            assertEquals(2, stack.getCount());
            assertEquals(bore.getId().toString(), stack.save(new CompoundTag()).getString("id"));
        }
        assertSame(oldConflict.get(), forge.getValue(oldConflict.getId()));
        assertSame(currentConflict.get(), forge.getValue(currentConflict.getId()));
        assertFalse(snapshot.aliases.containsKey(oldConflict.getId()));
        assertSame(Items.PAPER, forge.getHolder(oldId("port_alias_existing")).orElseThrow().value());
        assertEquals(oldId("port_alias_unregistered"), snapshot.aliases.get(oldId("port_alias_dangling")));
        assertTrue(forge.getHolder(oldId("port_alias_dangling")).isEmpty());
        assertSame(currentMissing.get(), forge.getValue(currentMissing.getId()));
        assertFalse(forge.containsKey(oldId("turret_bore")));
    }

    @Test
    void datapackRegistryReadsLegacyReferencesAndWritesOnlyCanonicalKeys() {
        ResourceKey<Registry<String>> registryKey = ResourceKey.createRegistryKey(TTIds.rl("port_alias_test"));
        var registry = new MappedRegistry<String>(registryKey, Lifecycle.stable());
        var canonicalKey = ResourceKey.create(registryKey, TTIds.rl("arcane_bore"));
        var oldKey = ResourceKey.create(registryKey, oldId("arcane_bore"));
        var conflict = ResourceKey.create(registryKey, oldId("present"));
        var currentConflict = ResourceKey.create(registryKey, TTIds.rl("present"));
        registry.register(canonicalKey, "current", Lifecycle.stable());
        registry.register(conflict, "authored legacy entry", Lifecycle.stable());
        registry.register(currentConflict, "authored current entry", Lifecycle.stable());
        registry.freeze();

        assertTrue(registry.containsKey(oldKey));
        assertTrue(registry.containsKey(oldKey.location()));
        assertEquals("current", registry.get(oldKey));
        assertEquals("current", registry.get(oldKey.location()));
        assertEquals("authored legacy entry", registry.get(conflict));
        assertFalse(registry.containsKey(TTIds.rl("turret_bore")));
        assertFalse(registry.containsKey(oldId("missing")));
        assertEquals(Set.of(canonicalKey.location(), conflict.location(), currentConflict.location()), registry.keySet());

        var ops = RegistryOps.create(JsonOps.INSTANCE, new RegistryAccess.ImmutableRegistryAccess(List.of(registry)));
        var codec = RegistryFixedCodec.create(registryKey);
        var decoded = codec.parse(ops, new JsonPrimitive(oldKey.location().toString())).getOrThrow(false, message -> fail(message));
        assertSame(registry.getHolderOrThrow(canonicalKey), decoded);
        assertEquals(new JsonPrimitive(canonicalKey.location().toString()),
                codec.encodeStart(ops, decoded).getOrThrow(false, message -> fail(message)));
    }
}
