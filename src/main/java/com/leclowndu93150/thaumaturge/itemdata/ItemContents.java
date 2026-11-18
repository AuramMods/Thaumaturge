package com.leclowndu93150.thaumaturge.itemdata;

import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** An immutable, slot-preserving inventory snapshot for focus pouches. */
public final class ItemContents {
    public static final int MAX_SLOTS = 256;
    private record Slot(int slot, ItemStack item) {}
    private static final Codec<Slot> SLOT_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, MAX_SLOTS - 1).fieldOf("slot").forGetter(Slot::slot),
            ItemStackCodecs.CODEC.fieldOf("item").forGetter(Slot::item)).apply(i, Slot::new));
    public static final Codec<ItemContents> CODEC = SLOT_CODEC.listOf().flatXmap(ItemContents::decode,
            value -> DataResult.success(value.slots()));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemContents> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    private final List<ItemStack> items;

    private ItemContents(List<ItemStack> items) { this.items = items; }

    public static ItemContents fromItems(List<ItemStack> items) {
        if (items.size() > MAX_SLOTS) throw new IllegalArgumentException("Too many item slots: " + items.size());
        int size = items.size();
        while (size > 0 && items.get(size - 1).isEmpty()) size--;
        return new ItemContents(items.subList(0, size).stream().map(ItemStack::copy).toList());
    }

    public void copyInto(List<ItemStack> destination) {
        if (items.size() > destination.size()) throw new IllegalArgumentException("Item inventory does not fit destination");
        for (int i = 0; i < destination.size(); i++) {
            destination.set(i, i < items.size() ? items.get(i).copy() : ItemStack.EMPTY);
        }
    }

    private List<Slot> slots() {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) slots.add(new Slot(i, items.get(i)));
        }
        return slots;
    }

    private static DataResult<ItemContents> decode(List<Slot> slots) {
        HashSet<Integer> seen = new HashSet<>();
        int size = 0;
        for (Slot slot : slots) {
            if (!seen.add(slot.slot())) return DataResult.error(() -> "Duplicate item slot " + slot.slot());
            size = Math.max(size, slot.slot() + 1);
        }
        List<ItemStack> items = new ArrayList<>(java.util.Collections.nCopies(size, ItemStack.EMPTY));
        for (Slot slot : slots) items.set(slot.slot(), slot.item().copy());
        return DataResult.success(fromItems(items));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ItemContents contents) || items.size() != contents.items.size()) return false;
        for (int i = 0; i < items.size(); i++) {
            if (!ItemStack.matches(items.get(i), contents.items.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (ItemStack item : items) {
            int itemHash = 31 * item.getItem().hashCode() + item.getCount();
            itemHash = 31 * itemHash + java.util.Objects.hashCode(item.getTag());
            hash = 31 * hash + itemHash;
        }
        return hash;
    }
}
