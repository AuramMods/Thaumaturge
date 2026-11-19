package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import java.util.Set;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

public final class InfusionEnchantmentHelper {
    private InfusionEnchantmentHelper() {}

    public static InfusionEnchantments get(ItemStack stack) {
        return ItemData.getOrDefault(stack, InfusionEnchantments.ITEM_DATA, InfusionEnchantments.EMPTY);
    }

    public static int level(ItemStack stack, InfusionEnchantment enchantment) {
        return get(stack).level(enchantment);
    }

    public static boolean has(ItemStack stack, InfusionEnchantment enchantment) {
        return get(stack).has(enchantment);
    }

    public static List<InfusionEnchantment> list(ItemStack stack) {
        return List.copyOf(get(stack).levels().keySet());
    }

    public static void add(ItemStack stack, InfusionEnchantment enchantment, int level) {
        if (stack.isEmpty() || level > enchantment.maxLevel()) {
            return;
        }
        InfusionEnchantments current = get(stack);
        if (current.level(enchantment) >= level) {
            return;
        }
        ItemData.set(stack, InfusionEnchantments.ITEM_DATA, current.with(enchantment, level));
    }

    public static boolean canApply(ItemStack stack, InfusionEnchantment enchantment) {
        if (stack.isEmpty()) {
            return false;
        }
        Set<String> classes = enchantment.toolClasses();
        if (classes.contains("all")) {
            return true;
        }
        if (classes.contains("weapon") && dealsAttackDamage(stack)) {
            return true;
        }
        if (classes.contains("pickaxe") && stack.is(ItemTags.PICKAXES)) {
            return true;
        }
        if (classes.contains("axe") && stack.is(ItemTags.AXES)) {
            return true;
        }
        if (classes.contains("shovel") && stack.is(ItemTags.SHOVELS)) {
            return true;
        }
        if (classes.contains("hoe") && stack.is(ItemTags.HOES)) {
            return true;
        }
        if (stack.getItem() instanceof Equipable equipable) {
            String slot = armorSlotToken(equipable.getEquipmentSlot());
            if (classes.contains("armor") || classes.contains(slot)) {
                return true;
            }
        }
        return classes.contains("chargable") && RechargeAccess.isRechargeable(stack);
    }

    private static boolean dealsAttackDamage(ItemStack stack) {
        return stack.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE);
    }

    private static String armorSlotToken(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helm";
            case CHEST -> "chest";
            case LEGS -> "legs";
            case FEET -> "boots";
            default -> "none";
        };
    }
}
