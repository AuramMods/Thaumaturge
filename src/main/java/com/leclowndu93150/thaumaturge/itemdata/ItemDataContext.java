package com.leclowndu93150.thaumaturge.itemdata;

import com.mojang.serialization.DynamicOps;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.RegistryOpsLookup;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;

/** Registry context for item codecs; no registry or decoded item cache survives a world. */
public final class ItemDataContext {
    private static final ThreadLocal<DynamicOps<Tag>> SCOPED_OPS = new ThreadLocal<>();
    private ItemDataContext() {}

    public static <T> T withRegistries(HolderLookup.Provider registries, Supplier<T> action) {
        return withNbtOps(RegistryOps.create(NbtOps.INSTANCE, registries),
                () -> RegistryContext.withRegistries(registries, action));
    }

    public static <T> T withOps(DynamicOps<?> source, Supplier<T> action) {
        DynamicOps<Tag> nbt = source instanceof RegistryOps<?> registry
                ? RegistryOps.create(NbtOps.INSTANCE, ((RegistryOpsLookup) registry).thaumaturge$registryInfoLookup())
                : NbtOps.INSTANCE;
        return withNbtOps(nbt, action);
    }

    private static <T> T withNbtOps(DynamicOps<Tag> ops, Supplier<T> action) {
        DynamicOps<Tag> previous = SCOPED_OPS.get();
        SCOPED_OPS.set(ops);
        try { return action.get(); }
        finally {
            if (previous == null) SCOPED_OPS.remove();
            else SCOPED_OPS.set(previous);
        }
    }

    public static DynamicOps<Tag> ops() {
        DynamicOps<Tag> ops = SCOPED_OPS.get();
        return ops == null ? RegistryContext.ops() : ops;
    }
}
