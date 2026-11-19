package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;

final class EquipmentAspects {
    private static final int ASPECT_PER_POINT = 4;
    private static final int BOW_AVERSIO = 10;
    private static final int BOW_VOLATUS = 5;
    private static final int SWORD_BASE_MODIFIER = 2;
    private static final int WOOD_TIER_DURABILITY = 59;
    private static final int STONE_TIER_DURABILITY = 131;
    private static final int IRON_TIER_DURABILITY = 250;

    private EquipmentAspects() {}

    static AspectList bonusFor(Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        AspectList bonus = AspectList.EMPTY;
        if (item instanceof ArmorItem armor && isHumanoidArmor(armor)) {
            bonus = with(bonus, registries, TTAspects.PRAEMUNIO, armor.getDefense() * ASPECT_PER_POINT);
        }
        if (stack.is(ItemTags.SWORDS)) {
            int attack = (int) additiveAttackDamage(stack);
            if (attack > 0) {
                int amount = Math.max(1, attack - SWORD_BASE_MODIFIER) * ASPECT_PER_POINT;
                bonus = with(bonus, registries, TTAspects.AVERSIO, amount);
            }
        } else if (item instanceof ProjectileWeaponItem) {
            bonus = with(bonus, registries, TTAspects.AVERSIO, BOW_AVERSIO);
            bonus = with(bonus, registries, TTAspects.VOLATUS, BOW_VOLATUS);
        } else if (stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || item instanceof ShearsItem) {
            int durability = stack.getMaxDamage();
            int amount;
            if (durability <= WOOD_TIER_DURABILITY) {
                amount = ASPECT_PER_POINT;
            } else if (durability <= STONE_TIER_DURABILITY) {
                amount = 2 * ASPECT_PER_POINT;
            } else if (durability <= IRON_TIER_DURABILITY) {
                amount = 3 * ASPECT_PER_POINT;
            } else {
                amount = 4 * ASPECT_PER_POINT;
            }
            bonus = with(bonus, registries, TTAspects.INSTRUMENTUM, amount);
        }
        return bonus;
    }

    private static AspectList with(
            AspectList list, HolderLookup.Provider registries, ResourceKey<IAspect> key, int amount) {
        Holder<IAspect> aspect = Aspects.resolve(registries, key);
        if (aspect == null || amount <= 0) {
            return list;
        }
        return list.add(aspect, amount);
    }

    private static double additiveAttackDamage(ItemStack stack) {
        // Read the item's declared modifiers, as the baseline does before Forge-style stack events.
        // A single declaration can appear in multiple slots, including declarations that apply to every slot.
        Set<AttackModifier> counted = new HashSet<>();
        double total = 0.0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            var modifiers = stack.getItem().getAttributeModifiers(slot, stack).get(Attributes.ATTACK_DAMAGE);
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADDITION
                        && counted.add(new AttackModifier(modifier.getId(), modifier.getAmount()))) {
                    total += modifier.getAmount();
                }
            }
        }
        return total;
    }

    private static boolean isHumanoidArmor(ArmorItem armor) {
        return armor.getEquipmentSlot().getType() == EquipmentSlot.Type.ARMOR;
    }

    private record AttackModifier(UUID id, double amount) {}
}
