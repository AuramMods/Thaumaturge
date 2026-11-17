package com.leclowndu93150.thaumaturge.serialization;

import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Scoped registry ownership for native callbacks whose 1.20.1 signatures lack a lookup. */
public final class RegistryContext {
    private static final ThreadLocal<HolderLookup.Provider> SCOPED = new ThreadLocal<>();

    private RegistryContext() {}

    public static <T> T withRegistries(HolderLookup.Provider registries, Supplier<T> action) {
        HolderLookup.Provider previous = SCOPED.get();
        SCOPED.set(Objects.requireNonNull(registries));
        try { return action.get(); }
        finally {
            if (previous == null) SCOPED.remove();
            else SCOPED.set(previous);
        }
    }

    public static HolderLookup.Provider scoped() { return SCOPED.get(); }

    /** Explicit caller scopes take precedence; an attached world's actual lookup is next. */
    public static HolderLookup.Provider resolve(HolderLookup.Provider world) {
        HolderLookup.Provider registries = scoped();
        if (registries != null) return registries;
        if (world != null) return world;
        registries = runtime();
        return registries == null ? RegistryAccess.EMPTY : registries;
    }

    private static HolderLookup.Provider runtime() {
        if (EffectiveSide.get().isServer()) {
            var server = ServerLifecycleHooks.getCurrentServer();
            return server == null ? null : server.registryAccess();
        }
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientRegistryContext::registries);
    }

    public static DynamicOps<Tag> ops() {
        HolderLookup.Provider registries = scoped();
        if (registries == null) registries = runtime();
        // Scalar schemas can run before a world; registry-dependent schemas
        // retain their normal codec error without an actual registry context.
        return registries == null ? NbtOps.INSTANCE : RegistryOps.create(NbtOps.INSTANCE, registries);
    }
}
