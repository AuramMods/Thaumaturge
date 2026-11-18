package com.leclowndu93150.thaumaturge.datamap;

import com.mojang.datafixers.util.Either;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/** Applies ordered pack layers against the registry's current tag bindings. */
public final class DataMapAssembler {
    private DataMapAssembler() {}
    private record Sourced<R, T>(T value, Either<TagKey<R>, ResourceKey<R>> source) {}

    public static <R, T> Map<ResourceKey<R>, T> build(Registry<R> registry, DataMapType<R, T> type,
            List<DataMapFile<R, T>> layers, Consumer<String> errors) {
        Map<ResourceKey<R>, Sourced<R, T>> values = new LinkedHashMap<>();
        for (var layer : layers) {
            if (layer.replace()) values.clear();
            for (var addition : layer.values()) resolve(registry, addition.key(), true, errors, holder -> {
                var key = holder.unwrapKey().orElseThrow(); var old = values.get(key); var entry = addition.entry();
                T value = old == null || entry.replace() ? entry.value()
                        : type.merger().merge(registry, old.source(), old.value(), addition.key(), entry.value());
                values.put(key, new Sourced<>(value, addition.key()));
            });
            for (var removal : layer.removals()) resolve(registry, removal.key(), false, errors, holder -> {
                var key = holder.unwrapKey().orElseThrow(); var old = values.get(key);
                if (removal.remover().isEmpty()) values.remove(key);
                else if (old != null) {
                    var result = removal.remover().get().remove(old.value(), registry, old.source(), holder.value());
                    if (result.isEmpty()) values.remove(key);
                    else values.put(key, new Sourced<>(result.get(), old.source()));
                }
            });
        }
        Map<ResourceKey<R>, T> result = new LinkedHashMap<>(); values.forEach((key, value) -> result.put(key, value.value()));
        return Map.copyOf(result);
    }

    private static <R> void resolve(Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> selector,
            boolean required, Consumer<String> errors, Consumer<Holder<R>> action) {
        selector.ifLeft(tag -> registry.getTagOrEmpty(tag).forEach(action)).ifRight(key -> {
            var holder = registry.getHolder(key);
            if (holder.isPresent()) action.accept(holder.get());
            else if (required) errors.accept("Unknown data map object " + key.location() + " in " + registry.key().location());
        });
    }
}
