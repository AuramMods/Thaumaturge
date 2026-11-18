package com.leclowndu93150.thaumaturge.datamap;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

/** Full snapshots use named IDs so a client resolves values against its own registry owners. */
public final class DataMapSync {
    private static final int FRAGMENT_BYTES = 512 * 1024;
    private static final int SNAPSHOT_BYTES = 64 * 1024 * 1024;

    private DataMapSync() {}

    public static List<Fragment> snapshot(RegistryAccess registries, DataMapCatalog catalog) {
        var types = catalog.types().stream()
                .filter(type -> type.networkCodec() != null)
                .sorted(Comparator.comparing((DataMapType<?, ?> type) -> type.registryKey().location()).thenComparing(DataMapType::id))
                .toList();
        var buffer = new FriendlyByteBuf(Unpooled.buffer(4096, SNAPSHOT_BYTES));
        try {
            ByteBufCodecs.writeCount(buffer, types.size(), ByteBufCodecs.MAX_COLLECTION_SIZE);
            var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
            for (var type : types) writeMap(buffer, ops, registries, type);
            var fragments = new ArrayList<Fragment>();
            while (buffer.isReadable()) {
                byte[] bytes = new byte[Math.min(FRAGMENT_BYTES, buffer.readableBytes())];
                buffer.readBytes(bytes);
                fragments.add(new Fragment(fragments.size(), !buffer.isReadable(), bytes));
            }
            return List.copyOf(fragments);
        } finally {
            buffer.release();
        }
    }

    @SuppressWarnings("unchecked")
    private static <R, T> void writeMap(FriendlyByteBuf buffer, DynamicOps<JsonElement> ops,
            RegistryAccess registries, DataMapType<R, T> type) {
        var registry = registries.registry(type.registryKey());
        if (registry.isEmpty() && type.mandatorySync()) {
            throw new IllegalStateException("Missing registry for required data map " + type.id());
        }
        var values = registry.map(value -> ((RegistryDataMapAccess<R>) value).thaumaturge$dataMap(type)).orElseGet(Map::of);
        buffer.writeResourceLocation(type.registryKey().location());
        buffer.writeResourceLocation(type.id());
        buffer.writeBoolean(type.mandatorySync());
        ByteBufCodecs.writeCount(buffer, values.size(), ByteBufCodecs.MAX_COLLECTION_SIZE);
        var sorted = values.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().location())).toList();
        for (var entry : sorted) {
            var encoded = type.networkCodec().encodeStart(ops, entry.getValue()).getOrThrow(false, message -> {
                throw new IllegalArgumentException("Cannot encode " + type.id() + " for " + entry.getKey().location() + ": " + message);
            });
            buffer.writeResourceLocation(entry.getKey().location());
            buffer.writeUtf(encoded.toString());
        }
    }

    private record MapId(ResourceLocation root, ResourceLocation id) {}

    private static void apply(byte[] bytes, RegistryAccess registries, DataMapCatalog catalog) {
        var buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        var staged = new HashMap<Registry<?>, Map<DataMapType<?, ?>, Map<?, ?>>>();
        try {
            var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
            Set<MapId> seen = new HashSet<>();
            int count = ByteBufCodecs.readCount(buffer, ByteBufCodecs.MAX_COLLECTION_SIZE);
            for (int i = 0; i < count; ++i) {
                var root = buffer.readResourceLocation();
                var id = buffer.readResourceLocation();
                boolean required = buffer.readBoolean();
                if (!seen.add(new MapId(root, id))) throw new DecoderException("Duplicate synced data map " + root + "/" + id);
                var type = catalog.get(ResourceKey.createRegistryKey(root), id);
                if (type != null && type.networkCodec() == null) type = null;
                if (required && (type == null || registries.registry(type.registryKey()).isEmpty())) {
                    throw new DecoderException("Unsupported required data map " + root + "/" + id);
                }
                readMap(buffer, ops, registries, type, staged);
            }
            if (buffer.isReadable()) throw new DecoderException("Trailing bytes in data-map snapshot");
        } finally {
            buffer.release();
        }
        // A snapshot also clears locally known synchronized maps the server no longer supplies.
        for (var type : catalog.types()) {
            if (type.networkCodec() != null) {
                registries.registry(type.registryKey()).ifPresent(registry -> staged.computeIfAbsent(registry, key -> new LinkedHashMap<>()));
            }
        }
        staged.forEach(DataMapSync::publish);
        staged.keySet().forEach(registry -> MinecraftForge.EVENT_BUS.post(
                new DataMapsUpdatedEvent(registries, registry, DataMapsUpdatedEvent.UpdateCause.CLIENT_SYNC)));
    }

    private static <R, T> void readMap(FriendlyByteBuf buffer, DynamicOps<JsonElement> ops, RegistryAccess registries,
            DataMapType<R, T> type, Map<Registry<?>, Map<DataMapType<?, ?>, Map<?, ?>>> staged) {
        Registry<R> registry = type == null ? null : registries.registry(type.registryKey()).orElse(null);
        int count = ByteBufCodecs.readCount(buffer, ByteBufCodecs.MAX_COLLECTION_SIZE);
        Map<ResourceKey<R>, T> values = new LinkedHashMap<>();
        Set<ResourceLocation> seen = new HashSet<>();
        for (int i = 0; i < count; ++i) {
            ResourceLocation id = buffer.readResourceLocation();
            String json = buffer.readUtf();
            if (!seen.add(id)) throw new DecoderException("Duplicate data-map entry " + id);
            if (registry == null) continue;
            var key = ResourceKey.create(type.registryKey(), id);
            if (registry.getHolder(key).isEmpty()) throw new DecoderException("Unknown synced entry " + id + " in " + type.id());
            T value = type.networkCodec().parse(ops, JsonParser.parseString(json)).getOrThrow(false, message -> {
                throw new DecoderException("Cannot decode " + type.id() + " for " + id + ": " + message);
            });
            values.put(key, value);
        }
        if (registry != null) staged.computeIfAbsent(registry, key -> new LinkedHashMap<>()).put(type, values);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <R> void publish(Registry<R> registry, Map<DataMapType<?, ?>, Map<?, ?>> maps) {
        // Every map was constructed using this registry and its registered type in readMap.
        DataMaps.replace(registry, (Map) maps);
    }

    /** Fragments fit within the native clientbound custom-payload limit. */
    public record Fragment(int index, boolean last, byte[] bytes) {
        public static Fragment read(FriendlyByteBuf buffer) {
            int index = ByteBufCodecs.readCount(buffer, SNAPSHOT_BYTES / FRAGMENT_BYTES - 1);
            boolean last = buffer.readBoolean();
            byte[] bytes = buffer.readByteArray(FRAGMENT_BYTES);
            if (buffer.isReadable() || bytes.length == 0 || (!last && bytes.length != FRAGMENT_BYTES)) {
                throw new DecoderException("Invalid data-map fragment");
            }
            return new Fragment(index, last, bytes);
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(index);
            buffer.writeBoolean(last);
            buffer.writeByteArray(bytes);
        }
    }

    /** Main-thread state owned by one remote connection. Tags are the commit boundary for a batch. */
    public static final class Receiver {
        private final DataMapCatalog catalog;
        private Connection connection;
        private RegistryAccess owner;
        private ByteArrayOutputStream pending;
        private int nextIndex;
        private boolean complete;

        public Receiver(DataMapCatalog catalog) {
            this.catalog = catalog;
        }

        public void receive(Connection from, Fragment fragment) {
            if (from.isMemoryConnection()) return;
            if (connection != from) {
                clear(connection);
                connection = from;
            }
            if (complete || fragment.index() != nextIndex || (nextIndex + 1L) * FRAGMENT_BYTES > SNAPSHOT_BYTES) {
                throw new DecoderException("Out-of-order data-map fragment " + fragment.index());
            }
            if (pending == null) pending = new ByteArrayOutputStream();
            pending.writeBytes(fragment.bytes());
            ++nextIndex;
            complete = fragment.last();
        }

        public void tagsApplied(Connection from, RegistryAccess registries) {
            if (connection != from || pending == null) return;
            if (!complete) throw new DecoderException("Incomplete data-map snapshot before tags");
            byte[] bytes = pending.toByteArray();
            resetBatch();
            if (owner != null && owner != registries) DataMaps.clear(owner);
            owner = registries;
            apply(bytes, registries, catalog);
        }

        public void clear(Connection from) {
            if (connection != from) return;
            if (owner != null) DataMaps.clear(owner);
            owner = null;
            connection = null;
            resetBatch();
        }

        private void resetBatch() {
            pending = null;
            nextIndex = 0;
            complete = false;
        }
    }
}
