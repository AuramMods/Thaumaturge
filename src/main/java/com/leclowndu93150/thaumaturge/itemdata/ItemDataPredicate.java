package com.leclowndu93150.thaumaturge.itemdata;

import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Positive component subset predicate for aspect variants. */
public record ItemDataPredicate(ItemDataPatch expected) {
    public static final ItemDataPredicate EMPTY = new ItemDataPredicate(ItemDataPatch.EMPTY);
    public static final Codec<ItemDataPredicate> CODEC = codec(ItemData::key);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemDataPredicate> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public ItemDataPredicate {
        if (expected.entrySet().stream().anyMatch(e -> e.getValue().isEmpty())) {
            throw new IllegalArgumentException("Item predicate cannot contain removals");
        }
    }

    public boolean test(ItemStack stack) { return expected.test(stack); }
    public boolean isEmpty() { return expected.isEmpty(); }
    public static Builder builder() { return new Builder(); }

    public static Codec<ItemDataPredicate> codec(Function<ResourceLocation, ItemDataKey<?>> lookup) {
        return ItemDataPatch.codec(lookup).flatXmap(
                patch -> patch.entrySet().stream().anyMatch(e -> e.getValue().isEmpty())
                        ? DataResult.error(() -> "Item predicate cannot contain removals")
                        : DataResult.success(new ItemDataPredicate(patch)),
                predicate -> DataResult.success(predicate.expected));
    }

    public static final class Builder {
        private final ItemDataPatch.Builder patch = ItemDataPatch.builder();
        public <T> Builder expect(ItemDataKey<T> key, T value) { patch.set(key, value); return this; }
        public ItemDataPredicate build() { return new ItemDataPredicate(patch.build()); }
    }
}
