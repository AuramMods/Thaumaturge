package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.loot.DifficultyValue;
import com.leclowndu93150.thaumaturge.content.entity.loot.LabyrinthBoundCondition;
import com.leclowndu93150.thaumaturge.content.entity.loot.NoNearbyKinCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTLootTypes {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, TTIds.MODID);
    public static final RegistryObject<LootItemConditionType> LABYRINTH_BOUND =
            CONDITIONS.register("labyrinth_bound", () -> new LootItemConditionType(LabyrinthBoundCondition.MAP_CODEC));
    public static final RegistryObject<LootItemConditionType> NO_NEARBY_KIN =
            CONDITIONS.register("no_nearby_kin", () -> new LootItemConditionType(NoNearbyKinCondition.MAP_CODEC));
    public static final DeferredRegister<LootNumberProviderType> NUMBER_PROVIDERS =
            DeferredRegister.create(Registries.LOOT_NUMBER_PROVIDER_TYPE, TTIds.MODID);
    public static final RegistryObject<LootNumberProviderType> BY_DIFFICULTY =
            NUMBER_PROVIDERS.register("by_difficulty", () -> new LootNumberProviderType(DifficultyValue.MAP_CODEC));

    private TTLootTypes() {}

    public static void register(IEventBus modBus) {
        com.leclowndu93150.thaumaturge.itemdata.BlockItemDataLoot.register(modBus);
        CONDITIONS.register(modBus);
        NUMBER_PROVIDERS.register(modBus);
    }
}
