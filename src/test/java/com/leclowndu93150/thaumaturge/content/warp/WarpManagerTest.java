package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class WarpManagerTest {
    @Test
    void onlyHumanoidArmorSlotsContributeAsPlayerArmor() {
        assertTrue(WarpManager.isHumanoidArmorSlot(EquipmentSlot.HEAD));
        assertTrue(WarpManager.isHumanoidArmorSlot(EquipmentSlot.CHEST));
        assertTrue(WarpManager.isHumanoidArmorSlot(EquipmentSlot.LEGS));
        assertTrue(WarpManager.isHumanoidArmorSlot(EquipmentSlot.FEET));
        assertFalse(WarpManager.isHumanoidArmorSlot(EquipmentSlot.MAINHAND));
        assertFalse(WarpManager.isHumanoidArmorSlot(EquipmentSlot.OFFHAND));
    }

    @Test
    void finalGearWarpIncludesOnlyPassiveGearAndStackWarp() {
        ItemStack stack = mock(ItemStack.class);
        LivingEntity wearer = mock(LivingEntity.class);
        Item item = mock(Item.class, withSettings().extraInterfaces(IWarpingGear.class));
        IWarpingGear gear = (IWarpingGear) item;

        when(stack.isEmpty()).thenReturn(false);
        when(stack.getItem()).thenReturn(item);
        ItemStack saved = new ItemStack(Items.STICK);
        ItemData.set(saved, TTDataComponents.STACK_WARP.get(), 3);
        when(stack.getTag()).thenReturn(saved.getTag());
        when(gear.warp(stack, wearer)).thenReturn(2);

        assertEquals(5, WarpManager.getFinalWarp(stack, wearer));
        verify(gear).warp(stack, wearer);
        verifyNoMoreInteractions(item);
    }
}
