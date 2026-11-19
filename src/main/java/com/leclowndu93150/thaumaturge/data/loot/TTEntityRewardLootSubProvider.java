package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.content.entity.loot.LootingChanceValue;
import com.leclowndu93150.thaumaturge.content.entity.loot.NumberChanceCondition;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class TTEntityRewardLootSubProvider implements LootTableSubProvider {
    private static final float CHAMPION_RARE_PER_LEVEL = 1.0F / 9.0F;
    private static final float CHAMPION_UNCOMMON_CHANCE = 4.0F / 9.0F;
    private static final List<Float> CHAMPION_UNCOMMON_BY_LEVEL =
            List.of(5.0F / 9.0F, 5.0F / 8.0F, 5.0F / 7.0F, 5.0F / 6.0F);

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(TTLootTables.CHAMPION_BAG.location(), championBag());
    }

    private LootTable.Builder championBag() {
        LootItemCondition.Builder rare = NumberChanceCondition.randomChance(
                LootingChanceValue.linear(0.0F, 0.0F, CHAMPION_RARE_PER_LEVEL));
        LootItemCondition.Builder uncommon = NumberChanceCondition.randomChance(
                LootingChanceValue.byLevel(CHAMPION_UNCOMMON_CHANCE, CHAMPION_UNCOMMON_BY_LEVEL, 1.0F));
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(TTItems.LOOT_BAG_RARE.get())
                                        .when(rare),
                                LootItem.lootTableItem(TTItems.LOOT_BAG_UNCOMMON.get())
                                        .when(uncommon),
                                LootItem.lootTableItem(TTItems.LOOT_BAG_COMMON.get()))));
    }
}
