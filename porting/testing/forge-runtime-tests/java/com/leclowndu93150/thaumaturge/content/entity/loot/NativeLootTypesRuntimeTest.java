package com.leclowndu93150.thaumaturge.content.entity.loot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterSettings;
import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.resource.PathPackResources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeLootTypesRuntimeTest {
    @BeforeAll
    static void registerTypes() throws ReflectiveOperationException {
        var bus = BusBuilder.builder().build();
        TTLootTypes.register(bus);
        register(bus, Registries.LOOT_CONDITION_TYPE, BuiltInRegistries.LOOT_CONDITION_TYPE);
        register(bus, Registries.LOOT_NUMBER_PROVIDER_TYPE, BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE);
    }

    private static <T> void register(IEventBus bus, ResourceKey<Registry<T>> key, Registry<T> registry)
            throws ReflectiveOperationException {
        var type = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        type.setAccessible(true);
        ((MappedRegistry<T>) registry).unfreeze();
        try {
            bus.post(type.newInstance(key, null, registry));
        } finally {
            registry.freeze();
        }
    }

    @Test
    void nativeLootJsonPreservesExistingTypeIdsFieldsAndValidation() {
        var gson = Deserializers.createLootTableSerializer().create();
        var source = """
                {"condition":"thaumaturge:number_chance","chance":{
                  "type":"thaumaturge:by_difficulty","values":{"hard":0.9},"default":0.6}}
                """;
        var chance = assertInstanceOf(NumberChanceCondition.class, gson.fromJson(source, LootItemCondition.class));
        assertEquals(DifficultyValue.of(Map.of(Difficulty.HARD, 0.9F), 0.6F), chance.chance());
        assertEquals(JsonParser.parseString(source), JsonParser.parseString(gson.toJson(chance, LootItemCondition.class)));
        assertThrows(JsonParseException.class, () -> gson.fromJson(
                "{\"condition\":\"thaumaturge:number_chance\",\"chance\":null}", LootItemCondition.class));
        var defaultValue = gson.fromJson("{\"type\":\"thaumaturge:by_difficulty\",\"values\":{}}", NumberProvider.class);
        assertEquals(0.0F, assertInstanceOf(DifficultyValue.class, defaultValue).fallback());

        var kinJson = "{\"condition\":\"thaumaturge:no_nearby_kin\",\"radius\":5.5}";
        var kin = assertInstanceOf(NoNearbyKinCondition.class, gson.fromJson(kinJson, LootItemCondition.class));
        assertEquals(5.5, kin.radius());
        assertEquals(JsonParser.parseString(kinJson), gson.toJsonTree(kin, LootItemCondition.class));
        assertThrows(JsonParseException.class,
                () -> gson.fromJson(kinJson.replace("5.5", "-1.0"), LootItemCondition.class));

        var labyrinthJson = "{\"condition\":\"thaumaturge:labyrinth_bound\"}";
        var labyrinth = assertInstanceOf(LabyrinthBoundCondition.class, gson.fromJson(labyrinthJson, LootItemCondition.class));
        assertEquals(JsonParser.parseString(labyrinthJson), gson.toJsonTree(labyrinth, LootItemCondition.class));
        assertFalse(labyrinth.test(mock(LootContext.class)));
    }

    @Test
    void chanceEvaluatesTheNumberProviderBeforeSamplingTheThreshold() {
        var context = mock(LootContext.class);
        var random = mock(RandomSource.class);
        var probability = mock(NumberProvider.class);
        when(context.getRandom()).thenReturn(random);
        when(random.nextFloat()).thenReturn(0.7F, 0.6F);
        when(probability.getFloat(context)).thenAnswer(call -> random.nextFloat());
        assertTrue(NumberChanceCondition.randomChance(probability).build().test(context));
        verify(random, times(2)).nextFloat();
    }

    @Test
    void nativeGsonPreservesChampionCurvesAndReadsLootingFromTheKiller() {
        var context = mock(LootContext.class);
        var gson = Deserializers.createLootTableSerializer().create();
        float uncommonBase = 4.0F / 9.0F;
        var rare = roundTripChance(gson, LootingChanceValue.linear(0.0F, 0.0F, 1.0F / 9.0F));
        var uncommon = roundTripChance(gson, LootingChanceValue.byLevel(
                uncommonBase, List.of(5.0F / 9.0F, 5.0F / 8.0F, 5.0F / 7.0F, 5.0F / 6.0F), 1.0F));

        assertEquals(0.0F, rare.chance().getFloat(context));
        assertEquals(uncommonBase, uncommon.chance().getFloat(context));
        when(context.getParamOrNull(LootContextParams.KILLER_ENTITY)).thenReturn(mock(Entity.class));
        assertEquals(uncommonBase, uncommon.chance().getFloat(context));

        var killer = mock(LivingEntity.class);
        when(context.getParamOrNull(LootContextParams.KILLER_ENTITY)).thenReturn(killer);
        var expectedRare = new float[] {0.0F, 0.0F, 1.0F / 9.0F, 2.0F / 9.0F, 3.0F / 9.0F, 4.0F / 9.0F};
        var expectedUncommon = new float[] {uncommonBase, 5.0F / 9.0F, 5.0F / 8.0F, 5.0F / 7.0F, 5.0F / 6.0F, 1.0F};
        for (int level = 0; level < expectedUncommon.length; level++) {
            equipLootingSword(killer, level);
            assertEquals(expectedRare[level], rare.chance().getFloat(context), 1.0e-7F, "rare level " + level);
            assertEquals(expectedUncommon[level], uncommon.chance().getFloat(context), "uncommon level " + level);
        }
    }

    @Test
    void ordinaryLootingChanceUsesTheSourceThresholdWithoutClamping() {
        var context = mock(LootContext.class);
        var killer = mock(LivingEntity.class);
        var random = mock(RandomSource.class);
        when(context.getParamOrNull(LootContextParams.KILLER_ENTITY)).thenReturn(killer);
        when(context.getRandom()).thenReturn(random);
        var chance = roundTripChance(Deserializers.createLootTableSerializer().create(),
                LootingChanceValue.increasing(0.25F, 0.125F));

        equipLootingSword(killer, 2);
        assertEquals(0.5F, chance.chance().getFloat(context));
        when(random.nextFloat()).thenReturn(Math.nextDown(0.5F), 0.5F, Math.nextDown(1.0F));
        assertTrue(chance.test(context));
        assertFalse(chance.test(context));

        equipLootingSword(killer, 7);
        assertEquals(1.125F, chance.chance().getFloat(context));
        assertTrue(chance.test(context));
        verify(random, times(3)).nextFloat();
    }

    @Test
    void nativeEquipmentTablesResolveWeightedKitReferencesAfterDatagenAndReload(@TempDir Path directory)
            throws Exception {
        var parent = id("knight");
        var special = id("knight/special");
        var fallback = id("knight/fallback");
        var diamond = id("knight/diamond");
        var gold = id("knight/gold");
        var provider = new LootTableProvider(new PackOutput(directory), Set.of(parent), List.of(
                new LootTableProvider.SubProviderEntry(() -> output -> {
                    output.accept(diamond, kit(Items.DIAMOND_SWORD, Items.DIAMOND_HELMET));
                    output.accept(gold, kit(Items.GOLDEN_SWORD, Items.GOLDEN_HELMET));
                    output.accept(fallback, kit(Items.IRON_SWORD, Items.IRON_HELMET));
                    output.accept(special, LootTable.lootTable().withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootTableReference.lootTableReference(diamond).setWeight(1))
                            .add(LootItem.lootTableItem(Items.GOLDEN_SWORD).setWeight(2))
                            .add(LootTableReference.lootTableReference(gold).setWeight(2))));
                    output.accept(parent, LootTable.lootTable().withPool(single(AlternativesEntry.alternatives(
                            LootTableReference.lootTableReference(special).when(NumberChanceCondition.randomChance(
                                    DifficultyValue.of(Map.of(Difficulty.HARD, 1.0F), 0.0F))),
                            LootTableReference.lootTableReference(fallback)))));
                }, MobSpawnEquipment.PARAMETERS)));
        provider.run(CachedOutput.NO_CACHE).join();

        var tables = new LootDataManager();
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources("equipment-references", false, directory)))) {
            SimpleReloadInstance.of(resources, List.of(tables), Runnable::run, Runnable::run,
                    CompletableFuture.completedFuture(Unit.INSTANCE)).done().get(10, TimeUnit.SECONDS);
        }
        var server = mock(MinecraftServer.class);
        var level = mock(ServerLevel.class);
        when(server.getLootData()).thenReturn(tables);
        when(level.getServer()).thenReturn(server);
        var loot = tables.getLootTable(parent);
        assertNotSame(LootTable.EMPTY, loot);
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.THIS_ENTITY, mock(Mob.class))
                .create(MobSpawnEquipment.PARAMETERS);

        when(level.getDifficulty()).thenReturn(Difficulty.NORMAL);
        assertEquals(List.of(Items.IRON_SWORD, Items.IRON_HELMET), roll(loot, params, 42));
        when(level.getDifficulty()).thenReturn(Difficulty.HARD);
        var observed = new HashSet<List<Item>>();
        for (long seed = 1; seed < 128; seed++) {
            observed.add(roll(loot, params, seed));
        }
        assertEquals(Set.of(List.of(Items.DIAMOND_SWORD, Items.DIAMOND_HELMET), List.of(Items.GOLDEN_SWORD),
                List.of(Items.GOLDEN_SWORD, Items.GOLDEN_HELMET)), observed);
    }

    @Test
    void encounterNamesAndAnnouncementsDecodeThroughNativeChatCodec() {
        var settings = EncounterSettings.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"name":"The Keeper","announcement":{"translate":"maze.test","color":"dark_purple"},
                 "reward":"thaumaturge:labyrinth/reward/key_room"}
                """)).result().orElseThrow();
        assertEquals("The Keeper", settings.name().getString());
        assertEquals("maze.test", assertInstanceOf(TranslatableContents.class, settings.announcement().getContents()).getKey());
        assertEquals(EncounterSettings.DEFAULT_VICTORY_DELAY, settings.victoryDelay());
    }

    private static NumberChanceCondition roundTripChance(com.google.gson.Gson gson, LootingChanceValue value) {
        var json = gson.toJson(NumberChanceCondition.randomChance(value).build(), LootItemCondition.class);
        assertEquals("thaumaturge:looting_chance",
                JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("chance").get("type").getAsString());
        var chance = assertInstanceOf(NumberChanceCondition.class, gson.fromJson(json, LootItemCondition.class));
        assertInstanceOf(LootingChanceValue.class, chance.chance());
        assertEquals(Set.of(LootContextParams.KILLER_ENTITY), chance.getReferencedContextParams());
        return chance;
    }

    private static void equipLootingSword(LivingEntity killer, int level) {
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        if (level > 0) {
            sword.enchant(Enchantments.MOB_LOOTING, level);
        }
        when(killer.getItemBySlot(EquipmentSlot.MAINHAND)).thenReturn(sword);
    }

    private static LootTable.Builder kit(Item weapon, Item helmet) {
        return LootTable.lootTable().withPool(single(LootItem.lootTableItem(weapon)))
                .withPool(single(LootItem.lootTableItem(helmet)));
    }

    private static LootPool.Builder single(LootPoolEntryContainer.Builder<?> entry) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(entry);
    }

    private static List<Item> roll(LootTable table, LootParams params, long seed) {
        var drops = new ArrayList<ItemStack>();
        table.getRandomItemsRaw(new LootContext.Builder(params).withOptionalRandomSeed(seed).create(null), drops::add);
        return drops.stream().map(ItemStack::getItem).toList();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("port_loot_types", path);
    }
}
