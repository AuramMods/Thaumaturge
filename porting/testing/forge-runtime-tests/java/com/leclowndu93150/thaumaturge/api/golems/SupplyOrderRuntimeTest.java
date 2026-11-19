package com.leclowndu93150.thaumaturge.api.golems;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class SupplyOrderRuntimeTest {
    @Test
    void hashUsesTheNativeItemAndTagEqualityIncludingEmptyStacks() {
        var level = mock(Level.class);
        var destination = SupplyOrder.toBlock(BlockPos.ZERO, Direction.UP);
        var wanted = new ItemStack(Items.IRON_INGOT, 2);
        wanted.getOrCreateTag().putInt("quality", 5);
        var first = new SupplyOrder(level, wanted, destination, 1);
        var second = new SupplyOrder(level, wanted, destination, 1);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        first.wanted().getOrCreateTag().putInt("quality", 6);
        assertNotEquals(first, second);
        first.wanted().setCount(0);
        second.wanted().setCount(0);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
