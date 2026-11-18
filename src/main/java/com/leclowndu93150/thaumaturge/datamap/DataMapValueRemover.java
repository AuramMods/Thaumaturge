package com.leclowndu93150.thaumaturge.datamap;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

@FunctionalInterface
public interface DataMapValueRemover<R, T> {
    Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, R object);
}
