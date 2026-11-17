package com.leclowndu93150.thaumaturge.itemdata;

import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Registry context for item codecs; no registry or decoded item cache survives a world. */
public final class ItemDataContext {
    private static final ThreadLocal<HolderLookup.Provider> SCOPED_REGISTRIES = new ThreadLocal<>();

    private ItemDataContext() {}

    public static <T> T withRegistries(HolderLookup.Provider registries, Supplier<T> action) {
        HolderLookup.Provider previous = SCOPED_REGISTRIES.get();
        SCOPED_REGISTRIES.set(Objects.requireNonNull(registries));
        try { return action.get(); }
        finally {
            if (previous == null) SCOPED_REGISTRIES.remove();
            else SCOPED_REGISTRIES.set(previous);
        }
    }

    public static DynamicOps<Tag> ops() {
        HolderLookup.Provider registries = SCOPED_REGISTRIES.get();
        if (registries == null) {
            if (EffectiveSide.get().isServer()) {
                var server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) registries = server.registryAccess();
            } else {
                registries = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientItemDataContext::registries);
            }
        }
        // Scalar/default schemas work before a world exists. A registry-dependent
        // schema reports its real codec error without a runtime or scoped lookup.
        return registries == null ? NbtOps.INSTANCE : RegistryOps.create(NbtOps.INSTANCE, registries);
    }
}
