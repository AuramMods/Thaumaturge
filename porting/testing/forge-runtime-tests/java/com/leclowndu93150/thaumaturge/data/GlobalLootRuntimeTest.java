package com.leclowndu93150.thaumaturge.data;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.leclowndu93150.thaumaturge.data.loot.TTGlobalLootModifierProvider;
import com.leclowndu93150.thaumaturge.registry.TTGlobalLootModifiers;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GlobalLootRuntimeTest {
    @BeforeAll
    static void registerLootTypes() throws ReflectiveOperationException {
        // Reproduce the Forge loot condition and mod codec registrations in the focused launcher.
        var conditions = (MappedRegistry<?>) BuiltInRegistries.LOOT_CONDITION_TYPE;
        var conditionId = ResourceLocation.fromNamespaceAndPath("forge", "loot_table_id");
        if (!conditions.containsKey(conditionId)) {
            conditions.unfreeze();
            try {
                Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, conditionId, LootTableIdCondition.LOOT_TABLE_ID);
            } finally {
                conditions.freeze();
            }
        }
        var bus = BusBuilder.builder().build();
        TTGlobalLootModifiers.register(bus);
        var registry = (ForgeRegistry<?>) ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get();
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        registry.unfreeze();
        try {
            bus.post(ctor.newInstance(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, registry, null));
        } finally {
            registry.freeze();
        }
    }

    @Test
    void sourceProviderGeneratesFiveDecodableAdditiveEntries(@TempDir Path directory) throws Exception {
        generate(directory);
        var index = read(directory.resolve("data/forge/loot_modifiers/global_loot_modifiers.json"));
        assertFalse(index.get("replace").getAsBoolean());
        List<String> entries = List.of(
                "treasure_common", "treasure_uncommon", "treasure_rare", "library_knowledge", "village_smith_quicksilver");
        assertEquals(entries.size(), index.getAsJsonArray("entries").size());
        for (String name : entries) {
            var json = modifier(directory, name);
            assertEquals("thaumaturge:add_table", json.get("type").getAsString());
            assertTrue(index.getAsJsonArray("entries").contains(new JsonPrimitive("thaumaturge:" + name)));
            assertNotNull(IGlobalLootModifier.DIRECT_CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow());
        }
        assertEquals(TTLootTables.TREASURE_LIBRARY.location().toString(), modifier(directory, "library_knowledge")
                .get("table").getAsString());
        assertEquals(TTLootTables.TREASURE_SMITH.location().toString(), modifier(directory, "village_smith_quicksilver")
                .get("table").getAsString());
    }

    @Test
    void decodedModifierRollsTheSubtableRawAndSplitsStacksOnlyForMatchingChests(@TempDir Path directory)
            throws Exception {
        generate(directory);
        var modifier = IGlobalLootModifier.DIRECT_CODEC.parse(JsonOps.INSTANCE, modifier(directory, "treasure_common"))
                .result().orElseThrow();
        var tableId = TTLootTables.TREASURE_COMMON.location();
        var oversizedDrops = mock(LootItemFunction.class);
        when(oversizedDrops.apply(any(ItemStack.class), any(LootContext.class))).thenAnswer(call -> {
            ItemStack stack = call.getArgument(0);
            stack.setCount(130);
            return stack;
        });
        var extra = spy(LootTable.lootTable().withPool(LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.IRON_INGOT).apply(() -> oversizedDrops)))
                .build());
        var server = mock(MinecraftServer.class);
        var level = mock(ServerLevel.class);
        var tables = mock(LootDataManager.class);
        when(server.getLootData()).thenReturn(tables);
        when(level.getServer()).thenReturn(server);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);
        when(tables.getElementOptional(LootDataType.TABLE, tableId)).thenReturn(Optional.of(extra));
        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .create(LootContextParamSets.CHEST);
        var untouched = new ItemStack(Items.GOLD_INGOT);
        var drops = new ObjectArrayList<>(List.of(untouched));
        var unrelated = chest(params, BuiltInLootTables.SPAWN_BONUS_CHEST);
        assertSame(drops, modifier.apply(drops, unrelated));
        verifyNoInteractions(tables, extra);

        var matching = chest(params, BuiltInLootTables.SIMPLE_DUNGEON);
        assertSame(drops, modifier.apply(drops, matching));
        assertSame(untouched, drops.get(0));
        assertEquals(List.of(64, 64, 2), drops.subList(1, drops.size()).stream().map(ItemStack::getCount).toList());
        assertTrue(drops.subList(1, drops.size()).stream().allMatch(stack -> stack.is(Items.IRON_INGOT)));
        verify(extra).getRandomItemsRaw(eq(matching), any());
        verify(extra, never()).getRandomItems(any(LootContext.class), any());

        when(tables.getElementOptional(LootDataType.TABLE, tableId)).thenReturn(Optional.empty());
        assertSame(drops, modifier.apply(drops, matching));
        assertEquals(4, drops.size());
    }

    private static LootContext chest(LootParams params, ResourceLocation id) {
        return new LootContext.Builder(params).withOptionalRandomSeed(42L).withQueriedLootTableId(id).create(null);
    }

    private static void generate(Path directory) {
        new TTGlobalLootModifierProvider(new PackOutput(directory)).run(CachedOutput.NO_CACHE).join();
    }

    private static JsonObject modifier(Path directory, String name) throws Exception {
        return read(directory.resolve("data/thaumaturge/loot_modifiers/" + name + ".json"));
    }

    private static JsonObject read(Path path) throws Exception {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
