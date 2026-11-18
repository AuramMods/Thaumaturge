package com.leclowndu93150.thaumaturge.content.entity.loot;

import java.util.EnumSet;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class MobSpawnEquipment {
    public static final LootContextParamSet PARAMETERS =
            LootContextParamSets.get(ResourceLocation.withDefaultNamespace("equipment"));
    private static final int MIN_ENCHANTMENT_COST = 5;
    private static final int MAX_EXTRA_COST = 17;
    private static final float WEAPON_ENCHANT_CHANCE = 0.25F;
    private static final float ARMOR_ENCHANT_CHANCE = 0.5F;

    private MobSpawnEquipment() {}

    public static void equip(Mob mob, ResourceKey<LootTable> tableId, Map<EquipmentSlot, Float> dropChances) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        LootTable table = level.getServer().getLootData().getLootTable(tableId.location());
        if (table == LootTable.EMPTY) {
            return;
        }
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .create(PARAMETERS);
        var equipped = EnumSet.noneOf(EquipmentSlot.class);
        for (ItemStack stack : table.getRandomItems(params)) {
            if (stack.isEmpty()) {
                continue;
            }
            Equipable item = Equipable.get(stack);
            EquipmentSlot slot = item == null ? EquipmentSlot.MAINHAND : item.getEquipmentSlot();
            if (equipped.add(slot)) {
                mob.setItemSlot(slot, slot.getType() == EquipmentSlot.Type.ARMOR ? stack.copyWithCount(1) : stack);
                Float dropChance = dropChances.get(slot);
                if (dropChance != null) {
                    mob.setDropChance(slot, dropChance);
                }
            }
        }
    }

    public static void enchantLoadout(Mob mob, RandomSource random, DifficultyInstance difficulty) {
        enchantWeapon(mob, random, difficulty);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                tryEnchantSlot(mob, slot, random, difficulty, ARMOR_ENCHANT_CHANCE);
            }
        }
    }

    public static void enchantWeapon(Mob mob, RandomSource random, DifficultyInstance difficulty) {
        tryEnchantSlot(mob, EquipmentSlot.MAINHAND, random, difficulty, WEAPON_ENCHANT_CHANCE);
    }

    private static void tryEnchantSlot(
            Mob mob, EquipmentSlot slot, RandomSource random, DifficultyInstance difficulty, float chance) {
        ItemStack stack = mob.getItemBySlot(slot);
        if (!stack.isEmpty() && random.nextFloat() < chance * difficulty.getSpecialMultiplier()) {
            enchant(stack, random, difficulty);
            mob.setItemSlot(slot, stack);
        }
    }

    public static void enchant(ItemStack stack, RandomSource random, DifficultyInstance difficulty) {
        int maxCost = MIN_ENCHANTMENT_COST + (int) (difficulty.getSpecialMultiplier() * MAX_EXTRA_COST);
        int cost = Mth.nextInt(random, MIN_ENCHANTMENT_COST, maxCost);
        var selected = EnchantmentHelper.selectEnchantment(random, stack, cost, false);
        if (!selected.isEmpty()) {
            var enchantments = EnchantmentHelper.getEnchantments(stack);
            for (var candidate : selected) {
                enchantments.merge(candidate.enchantment, candidate.level, Math::max);
            }
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
    }
}
