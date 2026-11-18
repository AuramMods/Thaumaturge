package com.leclowndu93150.thaumaturge.datamap;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedProfile;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaintedProfileRuntimeTest {
    private static final ResourceKey<MobTrait> TRAIT =
            ResourceKey.create(MobTrait.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", "flux_milk"));
    private static final String COW = """
            {"attributes":{"minecraft:generic.max_health":40.0,"minecraft:generic.attack_damage":6.0},
             "hunts_villagers":true,"hunts_animals":true,"traits":["thaumaturge:flux_milk"],
             "natural_spawns":true,"loot_table":"thaumaturge:entities/tainted/cow"}
            """;
    @TempDir Path pack;

    private RegistryAccess.Frozen owner() {
        var traits = new MappedRegistry<MobTrait>(MobTrait.REGISTRY_KEY, Lifecycle.stable());
        traits.register(TRAIT, new MobTrait() {}, Lifecycle.stable());
        return new RegistryAccess.ImmutableRegistryAccess(List.of(traits.freeze(), BuiltInRegistries.ENTITY_TYPE)).freeze();
    }

    @Test
    void profileRoundTripUsesNativeAttributeAndTraitRegistriesAndKeepsNamedLootReferences() {
        var owner = owner();
        var ops = RegistryOps.create(JsonOps.INSTANCE, owner);
        var source = JsonParser.parseString(COW);
        var profile = TaintedProfile.CODEC.parse(ops, source).result().orElseThrow();
        assertEquals(40.0, profile.attributes().get(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.MAX_HEALTH)));
        assertEquals(6.0, profile.attributes().get(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.ATTACK_DAMAGE)));
        assertTrue(profile.huntsVillagers());
        assertTrue(profile.huntsAnimals());
        assertTrue(profile.naturalSpawns());
        assertSame(owner.registryOrThrow(MobTrait.REGISTRY_KEY).getHolderOrThrow(TRAIT), profile.traits().get(0));
        assertSame(TTLootTables.TAINTED_COW, profile.lootTable().orElseThrow());
        assertEquals(source, TaintedProfile.CODEC.encodeStart(ops, profile).result().orElseThrow());

        var foreignOwner = RegistryOps.create(JsonOps.INSTANCE, owner());
        assertTrue(TaintedProfile.CODEC.encodeStart(foreignOwner, profile).error().isPresent());
        assertTrue(TaintedProfile.CODEC.parse(ops,
                JsonParser.parseString(COW.replace("minecraft:generic.max_health", "missing:attribute"))).error().isPresent());
    }

    @Test
    void nativePackLoadPublishesProfilesAndResolvesTheReferencedLootTable() throws Exception {
        var owner = owner();
        var catalog = new DataMapCatalog();
        catalog.register(TaintedProfile.DATA_MAP);
        catalog.freeze();
        var maps = new DataMapLoader(owner, ICondition.IContext.EMPTY, catalog);
        var loot = new LootDataManager();

        Path profiles = pack.resolve("data/thaumaturge/data_maps/entity_type/tainted_profile.json");
        Files.createDirectories(profiles.getParent());
        Files.writeString(profiles, "{\"values\":{\"minecraft:cow\":" + COW + "}}");
        Path cow = pack.resolve("data/thaumaturge/loot_tables/entities/tainted/cow.json");
        Files.createDirectories(cow.getParent());
        Files.writeString(cow, """
                {"type":"minecraft:entity",
                 "pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:beef"}]}]}
                """);

        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources("tainted-profile-fixture", pack, false)))) {
            SimpleReloadInstance.of(manager, List.of(maps, loot), Runnable::run, Runnable::run,
                    CompletableFuture.completedFuture(Unit.INSTANCE)).done().join();
            maps.apply(owner);
            assertTrue(maps.diagnostics().isEmpty(), maps.diagnostics().toString());
            var profile = TaintedProfile.of(EntityType.COW);
            assertNotNull(profile);
            assertSame(TTLootTables.TAINTED_COW, profile.lootTable().orElseThrow());
            assertNotSame(LootTable.EMPTY, loot.getLootTable(profile.lootTable().orElseThrow().location()));
            assertSame(LootTable.EMPTY, loot.getLootTable(TTLootTables.TAINTED_PIG.location()));
            assertNull(TaintedProfile.of(EntityType.PIG));
        } finally {
            DataMaps.clear(owner);
        }
        assertNull(TaintedProfile.of(EntityType.COW));
    }
}
