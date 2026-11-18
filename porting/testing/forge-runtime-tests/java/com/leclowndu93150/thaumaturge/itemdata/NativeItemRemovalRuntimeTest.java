package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

public class NativeItemRemovalRuntimeTest {
    @Test
    void removedDamageIsAbsentAndDisablesNativeDamageabilityAcrossCopyAndSaveLoad() {
        var stack = new ItemStack(Items.IRON_PICKAXE);
        stack.setDamageValue(12);
        assertEquals(12, ItemData.remove(stack, VanillaItemData.DAMAGE));
        assertNull(ItemData.get(stack, VanillaItemData.DAMAGE));
        assertFalse(stack.isDamageableItem());
        assertFalse(stack.copy().isDamageableItem());
        var loaded = ItemStack.of(stack.save(new CompoundTag()));
        assertNull(ItemData.get(loaded, VanillaItemData.DAMAGE));
        assertFalse(loaded.isDamageableItem());
    }

    @Test
    void nativeEnchantmentRemovalRemainsADistinctPatchFromTheEmptyDefault() {
        var stack = new ItemStack(Items.ENCHANTED_BOOK);
        var patch = ItemDataPatch.builder().remove(VanillaItemData.STORED_ENCHANTMENTS).build();
        patch.apply(stack);
        assertTrue(patch.test(stack));
        assertFalse(ItemData.has(stack, VanillaItemData.STORED_ENCHANTMENTS));
        assertNull(ItemData.get(ItemStack.of(stack.save(new CompoundTag())), VanillaItemData.STORED_ENCHANTMENTS));
    }

    @Test
    void laterNativeWritesRestorePresenceAndSetRestoresDamageability() {
        var stack = new ItemStack(Items.IRON_PICKAXE);
        ItemData.remove(stack, VanillaItemData.DAMAGE);
        stack.setDamageValue(8);
        assertTrue(stack.isDamageableItem());
        assertEquals(8, ItemData.get(stack, VanillaItemData.DAMAGE));
        ItemData.remove(stack, VanillaItemData.DAMAGE);
        ItemData.set(stack, VanillaItemData.DAMAGE, 3);
        assertTrue(stack.isDamageableItem());
        assertEquals(3, stack.getDamageValue());
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemData.remove(book, VanillaItemData.STORED_ENCHANTMENTS);
        book.getOrCreateTag().put("StoredEnchantments", new net.minecraft.nbt.ListTag());
        assertEquals(EnchantmentData.EMPTY, ItemData.get(book, VanillaItemData.STORED_ENCHANTMENTS));
    }

    @Test
    void nativeInventoryWireRetainsRemovedDamageWithoutChangingBufferOwnership() {
        var stack = new ItemStack(Items.IRON_PICKAXE);
        ItemData.remove(stack, VanillaItemData.DAMAGE);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeItem(stack);
            var decoded = buffer.readItem();
            assertFalse(decoded.isDamageableItem());
            assertNull(ItemData.get(decoded, VanillaItemData.DAMAGE));
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void copyingNativeChangesClearsStoredEffectsAndOmitsUnchangedDefaults() {
        var source = new ItemStack(Items.IRON_PICKAXE);
        var destination = new ItemStack(Items.IRON_PICKAXE);
        destination.setDamageValue(17);
        // The native constructor may store zero damage; it is still an unchanged default.
        ItemData.copyExplicitData(source, destination);
        assertEquals(17, destination.getDamageValue());
        ItemData.remove(source, VanillaItemData.DAMAGE);
        ItemData.copyExplicitData(source, destination);
        assertFalse(destination.isDamageableItem());
        ItemData.set(source, VanillaItemData.DAMAGE, 4);
        ItemData.copyExplicitData(source, destination);
        assertTrue(destination.isDamageableItem());
        assertEquals(4, destination.getDamageValue());
    }
}
