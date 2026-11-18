package com.leclowndu93150.thaumaturge.datamap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.RegistryObject;

/** Generates the retained data-map pack format using native Forge registries and cached output. */
public abstract class DataMapProvider implements DataProvider {
    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final Path output;
    private final Map<DataMapType<?, ?>, Builder<?, ?>> builders = new LinkedHashMap<>();

    protected DataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output.getOutputFolder(PackOutput.Target.DATA_PACK); this.lookupProvider = lookupProvider;
    }
    protected abstract void gather(HolderLookup.Provider provider);
    @Override public String getName() { return "Data Maps"; }
    @SuppressWarnings("unchecked")
    public <T, R> Builder<T, R> builder(DataMapType<R, T> type) {
        return (Builder<T, R>) builders.computeIfAbsent(type, key -> new Builder<>(type));
    }
    @Override public CompletableFuture<?> run(CachedOutput cache) {
        return lookupProvider.thenCompose(registries -> {
            builders.clear(); gather(registries);
            var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
            List<CompletableFuture<?>> writes = new ArrayList<>();
            builders.forEach((type, builder) -> {
                Path file = output.resolve(type.id().getNamespace()).resolve(DataMapLoader.folder(type.registryKey().location())).resolve(type.id().getPath() + ".json");
                JsonElement json = builder.encode(ops);
                writes.add(save(cache, json, file));
            });
            return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
        });
    }

    private static CompletableFuture<?> save(CachedOutput cache, JsonElement json, Path file) {
        // Native saveStable logs IO failures; retain its formatting/cache behavior and propagate the actual failure.
        var failure = new AtomicReference<IOException>();
        CachedOutput observed = (path, bytes, hash) -> {
            try { cache.writeIfNeeded(path, bytes, hash); }
            catch (IOException ex) { failure.set(ex); throw ex; }
        };
        return DataProvider.saveStable(observed, json, file).thenRun(() -> {
            if (failure.get() != null) throw new UncheckedIOException("Cannot write data map " + file, failure.get());
        });
    }

    public static final class Builder<T, R> {
        private record Value<T>(T value, boolean replace, List<ICondition> conditions) {}
        private record Removal<R, T>(String selector, DataMapValueRemover<R, T> remover) {}
        private final DataMapType<R, T> type;
        private final Map<String, Value<T>> values = new LinkedHashMap<>();
        private final List<Removal<R, T>> removals = new ArrayList<>();
        private final List<ICondition> conditions = new ArrayList<>();
        private boolean replace;
        public Builder(DataMapType<R, T> type) { this.type = type; }

        public Builder<T, R> add(ResourceLocation id, T value, boolean replace, ICondition... conditions) {
            values.put(id.toString(), new Value<>(value, replace, List.of(conditions))); return this;
        }
        public Builder<T, R> add(ResourceKey<R> key, T value, boolean replace, ICondition... conditions) {
            if (!key.isFor(type.registryKey())) throw new IllegalArgumentException("Entry belongs to another registry: " + key);
            return add(key.location(), value, replace, conditions);
        }
        public Builder<T, R> add(Holder<R> holder, T value, boolean replace, ICondition... conditions) {
            return add(holder.unwrapKey().orElseThrow(), value, replace, conditions);
        }
        public Builder<T, R> add(RegistryObject<? extends R> object, T value, boolean replace, ICondition... conditions) {
            return add(object.getId(), value, replace, conditions);
        }
        public Builder<T, R> add(TagKey<R> tag, T value, boolean replace, ICondition... conditions) {
            if (!tag.registry().equals(type.registryKey())) throw new IllegalArgumentException("Tag belongs to another registry: " + tag);
            values.put("#" + tag.location(), new Value<>(value, replace, List.of(conditions))); return this;
        }
        public Builder<T, R> remove(ResourceLocation id) { removals.add(new Removal<>(id.toString(), null)); return this; }
        public Builder<T, R> remove(TagKey<R> tag) { checkTag(tag); removals.add(new Removal<>("#" + tag.location(), null)); return this; }
        public Builder<T, R> remove(Holder<R> holder) { checkKey(holder); return remove(holder.unwrapKey().orElseThrow().location()); }
        public Builder<T, R> remove(ResourceLocation id, DataMapValueRemover<R, T> remover) { checkRemover(remover); removals.add(new Removal<>(id.toString(), remover)); return this; }
        public Builder<T, R> remove(TagKey<R> tag, DataMapValueRemover<R, T> remover) { checkTag(tag); checkRemover(remover); removals.add(new Removal<>("#" + tag.location(), remover)); return this; }
        public Builder<T, R> remove(Holder<R> holder, DataMapValueRemover<R, T> remover) { checkKey(holder); return remove(holder.unwrapKey().orElseThrow().location(), remover); }
        public Builder<T, R> replace(boolean replace) { this.replace = replace; return this; }
        public Builder<T, R> conditions(ICondition... conditions) { this.conditions.addAll(List.of(conditions)); return this; }
        private void checkTag(TagKey<R> tag) {
            if (!tag.registry().equals(type.registryKey())) throw new IllegalArgumentException("Tag belongs to another registry: " + tag);
        }
        private void checkKey(Holder<R> holder) {
            if (!holder.unwrapKey().orElseThrow().isFor(type.registryKey())) throw new IllegalArgumentException("Holder belongs to another registry: " + holder);
        }
        private void checkRemover(DataMapValueRemover<R, T> remover) {
            if (type.remover() == null || remover == null) throw new IllegalArgumentException("No targeted remover for " + type.id());
        }

        public JsonElement encode(DynamicOps<JsonElement> ops) {
            var file = new JsonObject(); if (replace) file.addProperty("replace", true);
            var entries = new JsonObject();
            values.forEach((selector, entry) -> {
                JsonElement json = encodeValue(type.codec(), ops, entry.value());
                if (entry.replace()) { var wrapped = new JsonObject(); wrapped.add("value", json); wrapped.addProperty("replace", true); json = wrapped; }
                entries.add(selector, conditioned(json, entry.conditions()));
            });
            file.add("values", entries);
            if (!removals.isEmpty()) {
                var remove = new JsonArray();
                for (var removal : removals) {
                    if (type.remover() == null) remove.add(removal.selector());
                    else {
                        var record = new JsonObject(); record.addProperty("key", removal.selector());
                        if (removal.remover() != null) record.add("remover", encodeRemover(ops, removal.remover()));
                        remove.add(record);
                    }
                }
                file.add("remove", remove);
            }
            return conditioned(file, conditions);
        }
        @SuppressWarnings("unchecked")
        private JsonElement encodeRemover(DynamicOps<JsonElement> ops, DataMapValueRemover<R, T> remover) {
            return encodeValue((Codec<DataMapValueRemover<R, T>>) type.remover(), ops, remover);
        }
        private <V> JsonElement encodeValue(Codec<V> codec, DynamicOps<JsonElement> ops, V value) {
            return codec.encodeStart(ops, value).getOrThrow(false, message -> { throw new IllegalArgumentException("Cannot encode data map " + type.id() + ": " + message); });
        }
        private JsonElement conditioned(JsonElement value, List<ICondition> conditions) {
            if (conditions.isEmpty()) return value;
            JsonObject object;
            if (value.isJsonObject()) {
                object = value.getAsJsonObject().deepCopy();
                if (object.has("forge:conditions") || object.has("forge:value")) throw new IllegalArgumentException("Reserved condition keys in " + type.id());
            } else { object = new JsonObject(); object.add("forge:value", value); }
            var terms = new JsonArray(); conditions.forEach(condition -> terms.add(CraftingHelper.serialize(condition)));
            object.add("forge:conditions", terms); return object;
        }
    }
}
