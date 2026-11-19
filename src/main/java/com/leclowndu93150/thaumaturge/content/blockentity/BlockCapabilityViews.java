package com.leclowndu93150.thaumaturge.content.blockentity;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jspecify.annotations.Nullable;

/** Owns the native sided views of a block entity without owning their storage. */
public final class BlockCapabilityViews implements ICapabilityProvider {
    private final Map<Capability<?>, Views<?>> providers = new IdentityHashMap<>();
    private boolean active = true;

    public <T> void expose(Capability<T> capability, Function<@Nullable Direction, @Nullable T> provider) {
        if (providers.putIfAbsent(capability, new Views<>(provider)) != null) {
            throw new IllegalArgumentException("Duplicate block capability " + capability.getName());
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        var views = providers.get(capability);
        return active && views != null ? views.get(side).cast() : LazyOptional.empty();
    }

    public void invalidate() {
        active = false;
        refresh();
    }

    public void revive() {
        active = true;
    }

    /** Closes the old views so consumers resolve a new side after the machine changes. */
    public void refresh() {
        providers.values().forEach(Views::invalidate);
    }

    private static final class Views<T> {
        private final Function<@Nullable Direction, @Nullable T> provider;
        private final Map<@Nullable Direction, LazyOptional<T>> bySide = new HashMap<>();

        Views(Function<@Nullable Direction, @Nullable T> provider) {
            this.provider = provider;
        }

        LazyOptional<T> get(@Nullable Direction side) {
            var cached = bySide.get(side);
            if (cached != null) {
                return cached;
            }
            T value = provider.apply(side);
            if (value == null) {
                return LazyOptional.empty();
            }
            var view = LazyOptional.of(() -> value);
            bySide.put(side, view);
            return view;
        }

        void invalidate() {
            var old = List.copyOf(bySide.values());
            bySide.clear();
            old.forEach(LazyOptional::invalidate);
        }
    }
}
