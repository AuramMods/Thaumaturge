package com.leclowndu93150.thaumaturge.itemdata;

import com.mojang.serialization.DynamicOps;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/** Registry context for item codecs; no registry or decoded item cache survives a world. */
public final class ItemDataContext {
    private ItemDataContext() {}

    public static <T> T withRegistries(HolderLookup.Provider registries, Supplier<T> action) {
        return RegistryContext.withRegistries(registries, action);
    }

    public static DynamicOps<Tag> ops() {
        return RegistryContext.ops();
    }
}
