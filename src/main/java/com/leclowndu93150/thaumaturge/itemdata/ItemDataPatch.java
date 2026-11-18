package com.leclowndu93150.thaumaturge.itemdata;

import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Named, typed item changes used by infusion recipes and research requirements. */
public final class ItemDataPatch {
    public static final ItemDataPatch EMPTY = new ItemDataPatch(Map.of());
    public static final Codec<ItemDataPatch> CODEC = codec(ItemData::key);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemDataPatch> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
    private final Map<ItemDataKey<?>, Optional<?>> values;

    private ItemDataPatch(Map<ItemDataKey<?>, Optional<?>> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public Set<Map.Entry<ItemDataKey<?>, Optional<?>>> entrySet() { return values.entrySet(); }
    public boolean isEmpty() { return values.isEmpty(); }
    @SuppressWarnings("unchecked")
    public <T> T get(ItemDataKey<T> key) { return (T) values.getOrDefault(key, Optional.empty()).orElse(null); }

    public ItemDataPatch select(ItemDataSelection selection) {
        Map<ItemDataKey<?>, Optional<?>> selected = new LinkedHashMap<>();
        values.forEach((key, value) -> { if (selection.includes(key.id())) selected.put(key, value); });
        return selected.isEmpty() ? EMPTY : new ItemDataPatch(selected);
    }
    public static Builder builder() { return new Builder(); }

    /** Apply to a private stack first, so a failed field cannot partially change the caller's stack. */
    public void apply(ItemStack stack) {
        if (isEmpty()) return;
        ItemStack changed = stack.copy();
        for (var entry : values.entrySet()) apply(changed, entry.getKey(), entry.getValue());
        stack.setTag(changed.getTag() == null ? null : changed.getTag().copy());
    }

    public boolean test(ItemStack stack) {
        for (var entry : values.entrySet()) {
            if (entry.getValue().isEmpty()) {
                if (ItemData.has(stack, entry.getKey())) return false;
            } else if (!Objects.equals(entry.getValue().get(), ItemData.get(stack, entry.getKey()))) return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <V> void apply(ItemStack stack, ItemDataKey<V> key, Optional<?> value) {
        if (value.isPresent()) ItemData.set(stack, key, (V) value.get());
        else ItemData.remove(stack, key);
    }

    /** The supplied lookup allows isolated codec tests and addon catalogs without a global registry. */
    public static Codec<ItemDataPatch> codec(Function<ResourceLocation, ItemDataKey<?>> lookup) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<ItemDataPatch, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getMap(input).flatMap(map -> {
                    Map<ItemDataKey<?>, Optional<?>> decoded = new LinkedHashMap<>();
                    for (Pair<T, T> entry : map.entries().toList()) {
                        DataResult<String> name = ops.getStringValue(entry.getFirst());
                        if (name.error().isPresent()) return DataResult.error(() -> "Item field name must be a string");
                        String field = name.result().orElseThrow();
                        boolean remove = field.startsWith("!");
                        ResourceLocation id = ResourceLocation.tryParse(remove ? field.substring(1) : field);
                        if (id == null) return DataResult.error(() -> "Invalid item field " + field);
                        ItemDataKey<?> key;
                        try { key = lookup.apply(id); }
                        catch (IllegalArgumentException ex) {
                            return DataResult.error(() -> "Cannot resolve item field " + id + ": " + ex.getMessage());
                        }
                        if (key == null) return DataResult.error(() -> "Unknown item field " + id);
                        if (decoded.containsKey(key)) return DataResult.error(() -> "Duplicate item field " + id);
                        if (remove) {
                            var marker = ops.getMap(entry.getSecond());
                            if (marker.error().isPresent() || marker.result().orElseThrow().entries().findAny().isPresent()) {
                                return DataResult.error(() -> "Item field removal must contain an empty object: " + field);
                            }
                            decoded.put(key, Optional.empty());
                        } else {
                            var value = key.codec().parse(ops, entry.getSecond());
                            if (value.error().isPresent()) {
                                return DataResult.error(() -> "Invalid " + id + ": " + value.error().orElseThrow().message());
                            }
                            decoded.put(key, Optional.of(value.result().orElseThrow()));
                        }
                    }
                    return DataResult.success(Pair.of(decoded.isEmpty() ? ItemDataPatch.EMPTY : new ItemDataPatch(decoded), ops.empty()));
                });
            }

            @Override
            public <T> DataResult<T> encode(ItemDataPatch patch, DynamicOps<T> ops, T prefix) {
                var builder = ops.mapBuilder();
                for (var entry : patch.values.entrySet()) {
                    if (entry.getValue().isPresent()) {
                        builder.add(ops.createString(entry.getKey().id().toString()), encodeValue(entry.getKey(), entry.getValue().get(), ops));
                    } else {
                        builder.add(ops.createString("!" + entry.getKey().id()), ops.emptyMap());
                    }
                }
                return builder.build(prefix);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T, V> DataResult<T> encodeValue(ItemDataKey<V> key, Object value, DynamicOps<T> ops) {
        return key.codec().encodeStart(ops, (V) value);
    }

    @Override public boolean equals(Object other) { return other instanceof ItemDataPatch patch && values.equals(patch.values); }
    @Override public int hashCode() { return values.hashCode(); }
    @Override public String toString() { return values.toString(); }

    public static final class Builder {
        private final Map<ItemDataKey<?>, Optional<?>> values = new LinkedHashMap<>();

        public <T> Builder set(ItemDataKey<T> key, T value) {
            values.put(Objects.requireNonNull(key), Optional.of(Objects.requireNonNull(value)));
            return this;
        }

        public Builder remove(ItemDataKey<?> key) {
            values.put(Objects.requireNonNull(key), Optional.empty());
            return this;
        }

        public ItemDataPatch build() { return values.isEmpty() ? EMPTY : new ItemDataPatch(values); }
    }
}
