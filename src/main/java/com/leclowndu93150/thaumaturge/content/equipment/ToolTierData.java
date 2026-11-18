package com.leclowndu93150.thaumaturge.content.equipment;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;

/** Source tag-based drop restrictions with native Forge tier identities and statistics. */
public final class ToolTierData implements Tier {
    public static final TagKey<Block> INCORRECT_FOR_DIAMOND_TOOL = tag("incorrect_for_diamond_tool");
    public static final TagKey<Block> INCORRECT_FOR_NETHERITE_TOOL = tag("incorrect_for_netherite_tool");
    private final int level;
    private final TagKey<Block> incorrectBlocks;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    private ToolTierData(int level, TagKey<Block> incorrectBlocks, int uses, float speed, float attackDamageBonus,
                         int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.incorrectBlocks = incorrectBlocks;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    private static TagKey<Block> tag(String path) { return TagKey.create(Registries.BLOCK, new ResourceLocation("minecraft", path)); }
    public static ToolTierData register(ResourceLocation id, int legacyLevel, TagKey<Block> incorrectBlocks, int uses,
                                        float speed, float attackDamageBonus, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        var tier = new ToolTierData(legacyLevel, incorrectBlocks, uses, speed, attackDamageBonus, enchantmentValue, repairIngredient);
        TierSortingRegistry.registerTier(tier, id, List.of(legacyLevel >= 4 ? Tiers.NETHERITE : Tiers.DIAMOND),
                legacyLevel >= 4 ? List.of() : List.of(Tiers.NETHERITE));
        return tier;
    }

    // Inherit the native null minimum-tier tag: these source tiers introduce only a blacklist.
    public TagKey<Block> getIncorrectBlocksForDrops() { return incorrectBlocks; }
    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return attackDamageBonus; }
    @Override public int getLevel() { return level; }
    @Override public int getEnchantmentValue() { return enchantmentValue; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
}
