package com.leclowndu93150.thaumaturge.content.equipment;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;

public final class PrimalCrusherItem extends Item {
    private static final int CRUSHER_WARP = 2;
    private static final Multimap<Attribute, AttributeModifier> MAIN_HAND_MODIFIERS = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier",
                    3.5F + TTMaterials.TOOL_PRIMAL_VOID.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier",
                    -2.8F, AttributeModifier.Operation.ADDITION));

    public PrimalCrusherItem(Properties properties) {
        super(GearWarp.with(properties, CRUSHER_WARP));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? MAIN_HAND_MODIFIERS : super.getDefaultAttributeModifiers(slot);
    }

    private static boolean canMine(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return canMine(state) ? TTMaterials.TOOL_PRIMAL_VOID.getSpeed() : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return canMine(state) && TierSortingRegistry.isCorrectTierForDrops(TTMaterials.TOOL_PRIMAL_VOID, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, miner, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return TTMaterials.TOOL_PRIMAL_VOID.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return TTMaterials.TOOL_PRIMAL_VOID.getRepairIngredient().test(repairCandidate);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) {
            VoidGearItem.selfRepairTick(stack, entity);
        }
    }
}
