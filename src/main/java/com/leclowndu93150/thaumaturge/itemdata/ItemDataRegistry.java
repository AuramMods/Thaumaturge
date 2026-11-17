package com.leclowndu93150.thaumaturge.itemdata;

import com.google.common.base.Suppliers;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.resources.ResourceLocation;

/** Local schema catalog; Forge 1.20.1 has no vanilla data-component registry. */
public final class ItemDataRegistry {
    private final String namespace;
    private final Map<ResourceLocation, Supplier<? extends ItemDataKey<?>>> keys = new LinkedHashMap<>();

    public ItemDataRegistry(String namespace) { this.namespace = namespace; }

    public <T> Supplier<ItemDataKey<T>> registerComponentType(String path, UnaryOperator<ItemDataKey.Builder<T>> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        // Resolve codecs lazily, just as the original deferred registration did.
        Supplier<ItemDataKey<T>> key = Suppliers.memoize(() -> new ItemDataKey<>(id, factory.apply(ItemDataKey.builder())));
        if (keys.putIfAbsent(id, key) != null) throw new IllegalArgumentException("Duplicate item data key " + id);
        return key;
    }

    public ItemDataKey<?> get(ResourceLocation id) {
        Supplier<? extends ItemDataKey<?>> key = keys.get(id);
        if (key == null) throw new IllegalArgumentException("Unknown item data key " + id);
        return key.get();
    }

    public Collection<ResourceLocation> ids() { return java.util.List.copyOf(keys.keySet()); }
}
