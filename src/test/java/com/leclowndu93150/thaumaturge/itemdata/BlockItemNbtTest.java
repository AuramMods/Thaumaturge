package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class BlockItemNbtTest {
    private final ResourceLocation contents = ResourceLocation.parse("thaumaturge:essentia_contents");
    private final ResourceLocation extra = ResourceLocation.parse("addon:extra");

    @Test
    void captureRetainsOnlyPositiveUnconsumedChangesAndNeverBlockEntityData() {
        CompoundTag stack = tag();
        CompoundTag fields = stack.getCompound(ItemData.TAG);
        fields.putInt(contents.toString(), 40);
        fields.putInt(extra.toString(), 8);
        fields.putBoolean("!thaumaturge:warp", true);
        stack.put("BlockEntityTag", new CompoundTag());
        CompoundTag blockState = new CompoundTag();
        blockState.putString("facing", "north");
        stack.put("BlockStateTag", blockState);
        CompoundTag saved = BlockItemNbt.capture(stack, Set.of(contents));
        assertFalse(saved.contains("BlockEntityTag"));
        assertEquals(blockState, saved.getCompound("BlockStateTag"));
        assertFalse(saved.getCompound(ItemData.TAG).contains(contents.toString()));
        assertFalse(saved.getCompound(ItemData.TAG).contains("!thaumaturge:warp"));
        assertEquals(8, saved.getCompound(ItemData.TAG).getInt(extra.toString()));
        assertEquals(40, fields.getInt(contents.toString()));
        assertTrue(fields.getBoolean("!thaumaturge:warp"));
    }

    @Test
    void namesAndUnknownDataSurviveCaptureAndActualNbtReloadIndependently() throws Exception {
        CompoundTag stack = tag();
        CompoundTag display = new CompoundTag();
        display.putString("Name", "named jar");
        stack.put("display", display);
        stack.putString("addon:other", "keep");
        CompoundTag captured = BlockItemNbt.capture(stack, Set.of());
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        net.minecraft.nbt.NbtIo.writeCompressed(captured, bytes);
        CompoundTag loaded = net.minecraft.nbt.NbtIo.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()));
        assertEquals("named jar", loaded.getCompound("display").getString("Name"));
        assertEquals("keep", loaded.getString("addon:other"));
        display.putString("Name", "changed");
        assertEquals("named jar", captured.getCompound("display").getString("Name"));
        assertTrue(BlockItemNbt.capture(null, Set.of()).isEmpty());
    }

    @Test
    void explicitIncludesAndExcludesFilterExtraDataAsWellAsCollectedFields() {
        CompoundTag stack = tag();
        stack.getCompound(ItemData.TAG).putInt(extra.toString(), 8);
        stack.putString("addon:other", "custom data");
        CompoundTag display = new CompoundTag();
        display.putString("Name", "name");
        display.putInt("color", 0x123456);
        stack.put("display", display);
        var customName = ResourceLocation.parse("minecraft:custom_name");
        CompoundTag selected = BlockItemNbt.select(stack,
                new ItemDataSelection(Optional.of(Set.of(extra, customName)), Set.of()));
        assertEquals(8, selected.getCompound(ItemData.TAG).getInt(extra.toString()));
        assertEquals("name", selected.getCompound("display").getString("Name"));
        assertFalse(selected.getCompound("display").contains("color"));
        assertFalse(selected.contains("addon:other"));
        CompoundTag excluded = BlockItemNbt.select(stack, new ItemDataSelection(Optional.empty(), Set.of(extra, customName)));
        assertFalse(excluded.contains(ItemData.TAG));
        assertFalse(excluded.getCompound("display").contains("Name"));
        assertEquals(0x123456, excluded.getCompound("display").getInt("color"));
        assertEquals("custom data", excluded.getString("addon:other"));
    }

    @Test
    void consumedContentsCannotReappearFromThePreservedSnapshotAfterEmptying() {
        CompoundTag full = tag();
        full.getCompound(ItemData.TAG).putInt(contents.toString(), 64);
        CompoundTag saved = BlockItemNbt.capture(full, Set.of(contents));
        assertFalse(saved.contains(ItemData.TAG));
        CompoundTag droppedEmptyItem = new CompoundTag();
        ItemNbt.copyExplicit(saved, droppedEmptyItem);
        assertFalse(droppedEmptyItem.contains(ItemData.TAG));
    }

    @Test
    void hideFlagsFollowTheirSelectedFieldsAndOtherMetadataIsPreserved() {
        CompoundTag stack = tag();
        stack.putInt("HideFlags", 1 | 2 | 32 | 64 | 128 | 256);
        assertEquals(1 | 2 | 32 | 64 | 128 | 256, BlockItemNbt.select(stack, ItemDataSelection.ALL).getInt("HideFlags"));
        var selected = new ItemDataSelection(Optional.of(Set.of(ResourceLocation.parse("minecraft:stored_enchantments"))), Set.of());
        assertEquals(32, BlockItemNbt.select(stack, selected).getInt("HideFlags"));
        assertTrue(BlockItemNbt.select(stack, new ItemDataSelection(Optional.of(Set.of()), Set.of())).isEmpty());
    }

    @Test
    void invalidPreservedFieldsReportErrorsWithoutMutatingTheSource() {
        CompoundTag stack = tag();
        stack.getCompound(ItemData.TAG).putInt(contents.toString(), 3);
        stack.getCompound(ItemData.TAG).putBoolean("!" + contents, true);
        CompoundTag before = stack.copy();
        assertThrows(IllegalArgumentException.class, () -> BlockItemNbt.capture(stack, Set.of()));
        assertEquals(before, stack);
        stack.getCompound(ItemData.TAG).remove("!" + contents);
        stack.getCompound(ItemData.TAG).putInt("bad id", 1);
        assertThrows(IllegalArgumentException.class, () -> BlockItemNbt.select(stack, ItemDataSelection.ALL));
    }

    private CompoundTag tag() {
        CompoundTag tag = new CompoundTag();
        tag.put(ItemData.TAG, new CompoundTag());
        return tag;
    }
}
