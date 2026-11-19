package com.leclowndu93150.thaumaturge.content.item.equipment;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataDefaults;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GogglesItem extends ArmorItem implements IVisDiscountGear {
    public static final ResourceLocation ID = TTIds.rl("goggles_revealing");
    private static final Multimap<Attribute, AttributeModifier> HEAD_MODIFIERS = ImmutableMultimap.of(
            Attributes.ARMOR, NamedAttributeModifiers.create(
                    ID, 1, AttributeModifier.Operation.ADDITION),
            Attributes.ARMOR_TOUGHNESS, NamedAttributeModifiers.create(
                    ID, 1, AttributeModifier.Operation.ADDITION));

    public GogglesItem(Properties properties) {
        super(
                TTMaterials.ARMOR_GOGGLES,
                Type.HELMET,
                ItemDataDefaults.with(properties, TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE));
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD ? HEAD_MODIFIERS : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 5;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return TTItems.GOGGLES_ENCHANTMENT_VALUE;
    }
}
