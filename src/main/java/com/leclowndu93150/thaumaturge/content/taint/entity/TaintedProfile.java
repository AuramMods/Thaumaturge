package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public record TaintedProfile(
        Map<Holder<Attribute>, Double> attributes,
        boolean huntsVillagers,
        boolean huntsAnimals,
        List<Holder<MobTrait>> traits,
        boolean naturalSpawns,
        Optional<ResourceKey<LootTable>> lootTable) {
    public static final Codec<TaintedProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.optionalFieldOf(Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), Codec.DOUBLE), "attributes", Map.of())
                            .forGetter(TaintedProfile::attributes),
                    Codecs.optionalFieldOf(Codec.BOOL, "hunts_villagers", false).forGetter(TaintedProfile::huntsVillagers),
                    Codecs.optionalFieldOf(Codec.BOOL, "hunts_animals", false).forGetter(TaintedProfile::huntsAnimals),
                    Codecs.optionalFieldOf(RegistryFixedCodec.create(MobTrait.REGISTRY_KEY)
                            .listOf(), "traits", List.of())
                            .forGetter(TaintedProfile::traits),
                    Codecs.optionalFieldOf(Codec.BOOL, "natural_spawns", false).forGetter(TaintedProfile::naturalSpawns),
                    Codecs.optionalFieldOf(TTLootTables.KEY_CODEC, "loot_table")
                            .forGetter(TaintedProfile::lootTable))
            .apply(instance, TaintedProfile::new));

    public static final DataMapType<EntityType<?>, TaintedProfile> DATA_MAP =
            DataMapType.builder(TTIds.rl("tainted_profile"), Registries.ENTITY_TYPE, CODEC).build();

    public static @Nullable TaintedProfile of(EntityType<?> type) {
        return DataMaps.get(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), DATA_MAP);
    }
}
