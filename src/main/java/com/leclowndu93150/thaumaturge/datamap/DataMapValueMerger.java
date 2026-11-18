package com.leclowndu93150.thaumaturge.datamap;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

@FunctionalInterface
public interface DataMapValueMerger<R, T> {
    T merge(Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> first, T oldValue,
            Either<TagKey<R>, ResourceKey<R>> second, T newValue);
}
