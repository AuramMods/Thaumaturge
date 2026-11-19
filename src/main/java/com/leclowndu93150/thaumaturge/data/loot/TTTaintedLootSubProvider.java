package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.content.entity.loot.LootingChanceValue;
import com.leclowndu93150.thaumaturge.content.entity.loot.NumberChanceCondition;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.function.BiConsumer;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class TTTaintedLootSubProvider implements LootTableSubProvider {
    private static final float LIVESTOCK_CHANCE = 1.0F / 3.0F;
    private static final float VILLAGER_CHANCE = 0.5F;
    private static final float VILLAGER_COIN_CHANCE = 1.0F / 13.0F;
    private static final int CHICKEN_TENDRIL_WEIGHT = 3;

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(TTLootTables.TAINTED_COW.location(), LootTable.lootTable().withPool(taintPool(1)));
        output.accept(TTLootTables.TAINTED_CREEPER.location(), LootTable.lootTable().withPool(taintPool(1)));
        output.accept(
                TTLootTables.TAINTED_PIG.location(),
                LootTable.lootTable()
                        .withPool(taintPool(1).when(LootItemRandomChanceCondition.randomChance(LIVESTOCK_CHANCE))));
        output.accept(
                TTLootTables.TAINTED_SHEEP.location(),
                LootTable.lootTable()
                        .withPool(taintPool(1).when(LootItemRandomChanceCondition.randomChance(LIVESTOCK_CHANCE))));
        output.accept(TTLootTables.TAINTED_CHICKEN.location(),
                LootTable.lootTable().withPool(taintPool(CHICKEN_TENDRIL_WEIGHT)));
        output.accept(
                TTLootTables.TAINTED_VILLAGER.location(),
                LootTable.lootTable()
                        .withPool(taintPool(1).when(LootItemRandomChanceCondition.randomChance(VILLAGER_CHANCE)))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET))
                                .when(NumberChanceCondition.randomChance(
                                        LootingChanceValue.increasing(VILLAGER_COIN_CHANCE, VILLAGER_COIN_CHANCE)))));
    }

    private static LootPool.Builder taintPool(int tendrilWeight) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(TTItems.TAINTED_GOO.get()))
                .add(LootItem.lootTableItem(TTItems.TAINT_TENDRIL.get()).setWeight(tendrilWeight));
    }
}
