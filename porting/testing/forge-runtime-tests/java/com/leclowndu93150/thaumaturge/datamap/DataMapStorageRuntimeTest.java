package com.leclowndu93150.thaumaturge.datamap;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.content.aspect.AspectDataMapRegistration;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionDataMaps;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.SmelterDataMaps;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionDataMaps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.fml.event.IModBusEvent;
import org.junit.jupiter.api.Test;

public class DataMapStorageRuntimeTest {
    private static final ResourceKey<Registry<String>> REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation("port_data_maps", "values"));
    private static final ResourceKey<String> VALUE = ResourceKey.create(REGISTRY, new ResourceLocation("port_data_maps", "value"));
    private static final DataMapType<String, Integer> TYPE = DataMapType.builder(new ResourceLocation("port_data_maps", "value"), REGISTRY, Codec.INT).synced(Codec.INT, true).build();

    private Registry<String> registry(String value) {
        var registry = new MappedRegistry<String>(REGISTRY, Lifecycle.stable());
        registry.register(VALUE, value, Lifecycle.stable()); return registry.freeze();
    }

    @Test
    void actualMappedRegistriesAndReferenceHoldersKeepIndependentWorldValues() {
        var first = registry("first"); var second = registry("second");
        DataMaps.replace(first, Map.of(TYPE, Map.of(VALUE, 7)));
        assertEquals(7, DataMaps.get(first.getHolderOrThrow(VALUE), TYPE));
        assertNull(DataMaps.get(second.getHolderOrThrow(VALUE), TYPE));
        DataMaps.replace(second, Map.of(TYPE, Map.of(VALUE, 9)));
        assertEquals(7, DataMaps.get(first.getHolderOrThrow(VALUE), TYPE));
        assertEquals(9, DataMaps.get(second.getHolderOrThrow(VALUE), TYPE));
        assertNull(DataMaps.get(Holder.direct("first"), TYPE));
        DataMaps.replace(first, Map.of());
        assertNull(DataMaps.get(first.getHolderOrThrow(VALUE), TYPE));
        assertEquals(9, DataMaps.get(second.getHolderOrThrow(VALUE), TYPE));
    }

    @Test
    void publicationOwnsImmutableMapsAndInvalidReplacementDoesNotPartiallyChangeValues() {
        var registry = registry("value"); var entries = new HashMap<ResourceKey<String>, Integer>(); entries.put(VALUE, 7);
        DataMaps.replace(registry, Map.of(TYPE, entries)); entries.put(VALUE, 99);
        assertEquals(7, DataMaps.get(registry.getHolderOrThrow(VALUE), TYPE));
        var unknown = ResourceKey.create(REGISTRY, new ResourceLocation("port_data_maps", "missing"));
        assertThrows(IllegalArgumentException.class, () -> DataMaps.replace(registry, Map.of(TYPE, Map.of(unknown, 9))));
        var otherRegistry = ResourceKey.<String>createRegistryKey(new ResourceLocation("port_data_maps", "other"));
        var wrongType = DataMapType.builder(TYPE.id(), otherRegistry, Codec.INT).build();
        assertThrows(IllegalArgumentException.class, () -> DataMaps.replace(registry, Map.of(wrongType, Map.of(VALUE, 9))));
        assertEquals(7, DataMaps.get(registry.getHolderOrThrow(VALUE), TYPE));
        @SuppressWarnings("unchecked") var storage = (RegistryDataMapAccess<String>) registry;
        assertThrows(UnsupportedOperationException.class, () -> storage.thaumaturge$dataMap(TYPE).put(VALUE, 8));
    }

    @Test
    void forgeNamespacedWrapperAndItsActualItemHoldersUseTheSameStorage() {
        var type = DataMapType.builder(new ResourceLocation("port_data_maps", "item"), Registries.ITEM, Codec.INT).build();
        var key = Items.PAPER.builtInRegistryHolder().key();
        try {
            DataMaps.replace(BuiltInRegistries.ITEM, Map.of(type, Map.of(key, 42)));
            assertEquals(42, DataMaps.get(Items.PAPER.builtInRegistryHolder(), type));
            assertEquals(42, DataMaps.get(BuiltInRegistries.ITEM.wrapAsHolder(Items.PAPER), type));
            assertNull(DataMaps.get(Items.IRON_PICKAXE.builtInRegistryHolder(), type));
        } finally { DataMaps.replace(BuiltInRegistries.ITEM, Map.of()); }
        assertNull(DataMaps.get(Items.PAPER.builtInRegistryHolder(), type));
    }

    @Test
    void realForgeModBusRegistersNineActualProductionSchemasAndPreservesIdsAndSyncMetadata() {
        var catalog = new DataMapCatalog(); var bus = BusBuilder.builder().markerType(IModBusEvent.class).build();
        bus.addListener(AspectDataMapRegistration::onRegister);
        bus.addListener(InfernalBonus::onRegister);
        bus.addListener(InfusionDataMaps::onRegister);
        bus.addListener(SmelterDataMaps::onRegister);
        bus.addListener(ChampionDataMaps::onRegister);
        bus.addListener((RegisterDataMapTypesEvent event) -> event.register(FocusTier.DATA_MAP));
        bus.post(new RegisterDataMapTypesEvent(catalog)); catalog.freeze();
        assertEquals(9, catalog.types().size());
        assertSame(AspectDataMaps.BASE_ASPECTS, catalog.get(Registries.ITEM, new ResourceLocation("thaumaturge", "base_aspects")));
        assertSame(InfernalBonus.DATA_MAP, catalog.get(Registries.ITEM, new ResourceLocation("thaumaturge", "infernal_bonus")));
        assertEquals(Set.of("thaumaturge:base_aspects", "thaumaturge:entity_aspects", "thaumaturge:infernal_bonus", "thaumaturge:infusion_pillar_set", "thaumaturge:infusion_matrix_upgrade", "thaumaturge:infusion_pedestal", "thaumaturge:smelter_stats", "thaumaturge:champion_whitelist", "thaumaturge:spell_focus"),
                catalog.types().stream().map(type -> type.id().toString()).collect(Collectors.toSet()));
        assertNotNull(AspectDataMaps.BASE_ASPECTS.networkCodec()); assertFalse(AspectDataMaps.BASE_ASPECTS.mandatorySync());
        assertNull(ChampionDataMaps.CHAMPION_WHITELIST.networkCodec());
        assertThrows(IllegalStateException.class, () -> catalog.register(TYPE));
    }

    @Test
    void catalogRejectsDuplicateIdentityWithinARootButAllowsTheSameIdOnAnotherRoot() {
        var catalog = new DataMapCatalog(); catalog.register(TYPE);
        assertThrows(IllegalArgumentException.class, () -> catalog.register(TYPE));
        var itemType = DataMapType.builder(TYPE.id(), Registries.ITEM, Codec.INT).build(); catalog.register(itemType);
        assertEquals(2, catalog.types().size()); assertSame(TYPE, catalog.get(REGISTRY, TYPE.id()));
        assertSame(itemType, catalog.get(Registries.ITEM, TYPE.id()));
        assertSame(Codec.INT, TYPE.codec()); assertSame(Codec.INT, TYPE.networkCodec()); assertTrue(TYPE.mandatorySync());
    }

    @Test
    void actualInfernalBonusMergesAndTargetedRemoverKeepOtherBonusEntries() {
        var paper = InfernalBonus.builder(Items.PAPER).count(2).chance(0.5f).build();
        var diamond = InfernalBonus.builder(Items.DIAMOND).count(3).build();
        var registry = BuiltInRegistries.ITEM;
        var first = Either.<net.minecraft.tags.TagKey<net.minecraft.world.item.Item>, ResourceKey<net.minecraft.world.item.Item>>right(Items.PAPER.builtInRegistryHolder().key());
        var second = Either.<net.minecraft.tags.TagKey<net.minecraft.world.item.Item>, ResourceKey<net.minecraft.world.item.Item>>right(Items.DIAMOND.builtInRegistryHolder().key());
        var merged = InfernalBonus.DATA_MAP.merger().merge(registry, first, List.of(paper), second, List.of(diamond));
        assertEquals(List.of(paper, diamond), merged);
        var remover = InfernalBonus.DATA_MAP.remover().parse(JsonOps.INSTANCE, new com.google.gson.JsonPrimitive("minecraft:paper")).result().orElseThrow();
        assertEquals(List.of(diamond), remover.remove(merged, registry, first, Items.PAPER).orElseThrow());
        assertTrue(remover.remove(List.of(paper), registry, first, Items.PAPER).isEmpty());
        var encoded = InfernalBonus.DATA_MAP.codec().encodeStart(JsonOps.INSTANCE, merged).result().orElseThrow();
        var decoded = InfernalBonus.DATA_MAP.codec().parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        assertEquals(2, decoded.size()); assertEquals(2, decoded.get(0).count().sample(net.minecraft.util.RandomSource.create(1)));
        assertEquals(0.5f, decoded.get(0).chance());
    }
}
