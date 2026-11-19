package com.leclowndu93150.thaumaturge.network.transport;

import com.leclowndu93150.thaumaturge.serialization.RegistryOpsLookup;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/** Supplies the shared holders for registries decoded together in the vanilla login packet. */
public final class LoginRegistryCodec implements RegistryOps.RegistryInfoLookup {
    private static final ThreadLocal<LoginRegistryCodec> DECODING = new ThreadLocal<>();
    private static final RegistryAccess BUILTINS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static final RegistryOps.RegistryInfoLookup BUILTINS_WITH_PENDING_TAGS = new RegistryOps.RegistryInfoLookup() {
        @Override
        public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> key) {
            return BUILTINS.registry(key).map(registry -> withPendingTags(registry, registry.asLookup()));
        }
    };

    private final Map<ResourceKey<?>, MappedRegistry<?>> registries = new HashMap<>();

    private LoginRegistryCodec() {}

    public static RegistryAccess.Frozen includeBuiltins(RegistryAccess received) {
        Map<ResourceKey<?>, Registry<?>> combined = new LinkedHashMap<>();
        BUILTINS.registries().forEach(entry -> combined.put(entry.key(), entry.value()));
        received.registries().forEach(entry -> combined.put(entry.key(), entry.value()));
        return new RegistryAccess.ImmutableRegistryAccess(List.copyOf(combined.values())).freeze();
    }

    public static <T> RegistryOps<T> encodingOps(DynamicOps<T> builtins, RegistryAccess registries) {
        return withFallback(builtins, lookupOf(RegistryOps.create(builtins, registries)), lookupOf(builtins));
    }

    public static Codec<RegistryAccess> withRegistryReferences(Codec<RegistryAccess> codec) {
        return Codec.of(codec, new Decoder<>() {
            @Override
            public <T> DataResult<Pair<RegistryAccess, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getMap(input).flatMap(entries -> {
                    LoginRegistryCodec context = new LoginRegistryCodec();
                    entries.entries().forEach(entry -> ops.getStringValue(entry.getFirst()).result()
                            .map(ResourceLocation::tryParse)
                            .ifPresent(context::addRegistry));

                    LoginRegistryCodec previous = DECODING.get();
                    DECODING.set(context);
                    try {
                        return codec.decode(withFallback(ops, context, BUILTINS_WITH_PENDING_TAGS), input);
                    } finally {
                        if (previous == null) DECODING.remove();
                        else DECODING.set(previous);
                    }
                });
            }
        });
    }

    private static RegistryOps.RegistryInfoLookup lookupOf(DynamicOps<?> ops) {
        return ((RegistryOpsLookup) ops).thaumaturge$registryInfoLookup();
    }

    private static <T> RegistryOps<T> withFallback(DynamicOps<T> ops, RegistryOps.RegistryInfoLookup primary,
                                                   RegistryOps.RegistryInfoLookup fallback) {
        return RegistryOps.create(ops, new RegistryOps.RegistryInfoLookup() {
            @Override
            public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> key) {
                return primary.lookup(key).or(() -> fallback.lookup(key));
            }
        });
    }

    @Override
    public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> key) {
        MappedRegistry<E> registry = findRegistry(key);
        return Optional.ofNullable(registry).map(value -> withPendingTags(value, value.createRegistrationLookup()));
    }

    private static <E> RegistryOps.RegistryInfo<E> withPendingTags(Registry<E> registry, HolderGetter<E> elements) {
        HolderGetter<E> getter = new HolderGetter<>() {
            @Override
            public Optional<Holder.Reference<E>> get(ResourceKey<E> key) {
                return elements.get(key);
            }

            @Override
            public Optional<HolderSet.Named<E>> get(TagKey<E> tag) {
                // The following clientbound tag packet binds this same named holder set.
                return Optional.of(registry.getOrCreateTag(tag));
            }
        };
        return new RegistryOps.RegistryInfo<>(registry.holderOwner(), getter, registry.registryLifecycle());
    }

    private void addRegistry(ResourceLocation id) {
        ResourceKey<Registry<Object>> key = ResourceKey.createRegistryKey(id);
        registries.put(key, new MappedRegistry<>(key, Lifecycle.experimental()));
    }

    // A ResourceKey fixes the element type for both the native codec and its lookup.
    @SuppressWarnings("unchecked")
    private <E> MappedRegistry<E> findRegistry(ResourceKey<? extends Registry<? extends E>> key) {
        return (MappedRegistry<E>) registries.get(key);
    }

    public static <E> MappedRegistry<E> registryBeingDecoded(ResourceKey<? extends Registry<E>> key) {
        LoginRegistryCodec context = DECODING.get();
        return context == null ? null : context.findRegistry(key);
    }
}
