package com.leclowndu93150.thaumaturge.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

/** Per-owner lazy state, persistent NBT and clone semantics shared by Forge providers. */
public final class AttachmentStorage {
    private final AttachmentRegistry registry;
    private final Supplier<RegistryAccess> registries;
    private final Runnable changed;
    private final Map<AttachmentKey<?>, Object> values = new IdentityHashMap<>();
    private CompoundTag pending = new CompoundTag();

    public AttachmentStorage(AttachmentRegistry registry, Supplier<RegistryAccess> registries, Runnable changed) {
        this.registry = registry;
        this.registries = registries;
        this.changed = changed;
    }

    public boolean has(AttachmentKey<?> key) {
        return values.containsKey(key) || pending.contains(key.id().toString());
    }

    public <T> T get(AttachmentKey<T> key) {
        T value = getExisting(key);
        if (value == null) {
            value = Objects.requireNonNull(key.createDefault(), "Null default for " + key.id());
            values.put(key, value);
            if (key.persistent()) changed.run();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExisting(AttachmentKey<T> key) {
        Object value = values.get(key);
        if (value != null) return (T) value;
        Tag tag = pending.get(key.id().toString());
        if (tag == null || !key.persistent()) return null;
        var result = key.codec().parse(ops(), tag);
        T decoded = result.result().orElseThrow(() -> new IllegalStateException(
                "Cannot decode state " + key.id() + ": " + result.error().orElseThrow().message()));
        values.put(key, decoded);
        pending.remove(key.id().toString());
        return decoded;
    }

    public <T> T set(AttachmentKey<T> key, T value) {
        Objects.requireNonNull(value, "Null state for " + key.id());
        T previous = getExisting(key);
        values.put(key, value);
        pending.remove(key.id().toString());
        if (key.persistent()) changed.run();
        return previous;
    }

    public <T> T remove(AttachmentKey<T> key) {
        T previous = getExisting(key);
        boolean present = has(key);
        values.remove(key);
        pending.remove(key.id().toString());
        if (present && key.persistent()) changed.run();
        return previous;
    }

    public CompoundTag save() {
        // Preserve unknown schema entries for round trips instead of silently deleting them.
        CompoundTag result = pending.copy();
        for (AttachmentKey<?> key : List.copyOf(values.keySet())) {
            saveValue(result, key);
        }
        return result;
    }

    public void load(CompoundTag tag) {
        values.clear();
        pending = tag.copy();
    }

    public List<AttachmentKey<?>> presentKeys() {
        return registry.keys().stream().filter(this::has).toList();
    }

    public void copyPersistentTo(AttachmentStorage target, boolean death) {
        CompoundTag saved = save();
        for (AttachmentKey<?> key : registry.keys()) {
            if (key.persistent() && (!death || key.copyOnDeath()) && saved.contains(key.id().toString())) {
                target.values.remove(key);
                target.pending.put(key.id().toString(), saved.get(key.id().toString()).copy());
            }
        }
        target.changed.run();
    }

    private <T> void saveValue(CompoundTag result, AttachmentKey<T> key) {
        T value = getExisting(key);
        result.remove(key.id().toString());
        if (value != null && key.shouldSave(value)) {
            var encoded = key.codec().encodeStart(ops(), value);
            result.put(key.id().toString(), encoded.result().orElseThrow(() -> new IllegalStateException(
                    "Cannot encode state " + key.id() + ": " + encoded.error().orElseThrow().message())));
        }
    }

    private DynamicOps<Tag> ops() {
        return RegistryOps.create(NbtOps.INSTANCE, registries.get());
    }
}
