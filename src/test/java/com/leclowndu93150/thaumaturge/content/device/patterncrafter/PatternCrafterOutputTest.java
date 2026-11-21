package com.leclowndu93150.thaumaturge.content.device.patterncrafter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

class PatternCrafterOutputTest {
    @Test
    void resultAndContainerCannotReserveTheSameEmptySlot() {
        var receiver = new ItemStackHandler(1);
        var sugar = new ItemStack(Items.SUGAR);
        var bucket = new ItemStack(Items.BUCKET);

        assertFalse(BlockEntityPatternCrafter.outputsFit(receiver, sugar, List.of(bucket)));
        assertTrue(receiver.getStackInSlot(0).isEmpty());
        assertTrue(BlockEntityPatternCrafter.outputsFit(new ItemStackHandler(2), sugar, List.of(bucket)));
    }

    @Test
    void repeatedOutputsShareOnlyTheSpaceRemainingInEachStack() {
        var receiver = new ItemStackHandler(2);
        receiver.setStackInSlot(0, new ItemStack(Items.BUCKET, 15));
        receiver.setStackInSlot(1, new ItemStack(Items.STONE, 64));
        var sugar = new ItemStack(Items.SUGAR);
        var bucket = new ItemStack(Items.BUCKET);

        assertTrue(BlockEntityPatternCrafter.outputsFit(receiver, bucket, List.of()));
        assertFalse(BlockEntityPatternCrafter.outputsFit(receiver, bucket, List.of(bucket)));
        assertEquals(15, receiver.getStackInSlot(0).getCount());

        receiver.setStackInSlot(1, new ItemStack(Items.SUGAR, 63));
        assertTrue(BlockEntityPatternCrafter.outputsFit(receiver, sugar, List.of(bucket)));
        assertFalse(BlockEntityPatternCrafter.outputsFit(receiver, sugar, List.of(bucket, bucket)));
        assertEquals(63, receiver.getStackInSlot(1).getCount());
    }

    @Test
    void reservationUsesTheReceiversOwnSlotRules() {
        var receiver = new ItemStackHandler(2) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot == 0 ? stack.is(Items.BUCKET) : stack.is(Items.SUGAR);
            }
        };
        var sugar = new ItemStack(Items.SUGAR);
        var bucket = new ItemStack(Items.BUCKET);

        assertTrue(BlockEntityPatternCrafter.outputsFit(receiver, sugar, List.of(bucket)));
        assertFalse(BlockEntityPatternCrafter.outputsFit(receiver, sugar, List.of(new ItemStack(Items.STONE))));
        assertTrue(receiver.getStackInSlot(0).isEmpty());
        assertTrue(receiver.getStackInSlot(1).isEmpty());
    }
}
