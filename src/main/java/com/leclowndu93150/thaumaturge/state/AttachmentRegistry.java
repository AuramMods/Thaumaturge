package com.leclowndu93150.thaumaturge.state;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/** Mod-local state schemas; Forge persists these inside a single capability. */
public final class AttachmentRegistry {
    private final String namespace;
    private final Map<ResourceLocation, AttachmentKey<?>> keys = new LinkedHashMap<>();

    public AttachmentRegistry(String namespace) { this.namespace = namespace; }

    public <T> AttachmentKey<T> register(String name, AttachmentKey.Builder<T> builder) {
        ResourceLocation id = new ResourceLocation(namespace, name);
        AttachmentKey<T> key = new AttachmentKey<>(id, builder);
        if (keys.putIfAbsent(id, key) != null) {
            throw new IllegalStateException("Duplicate state key " + id);
        }
        return key;
    }

    public AttachmentKey<?> get(ResourceLocation id) { return keys.get(id); }
    public Collection<AttachmentKey<?>> keys() { return List.copyOf(keys.values()); }
}
