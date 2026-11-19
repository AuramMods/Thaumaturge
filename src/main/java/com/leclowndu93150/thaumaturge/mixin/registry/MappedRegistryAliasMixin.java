package com.leclowndu93150.thaumaturge.mixin.registry;

import com.leclowndu93150.thaumaturge.content.legacy.LegacyRegistryAliases;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Resolve legacy names against each vanilla registry's own entries, including synced datapack registries. */
@Mixin(MappedRegistry.class)
abstract class MappedRegistryAliasMixin<T> {
    @Shadow @Final private ResourceKey<? extends Registry<T>> key;
    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;

    @ModifyVariable(method = {
        "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;",
        "containsKey(Lnet/minecraft/resources/ResourceKey;)Z",
        "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"
    }, at = @At("HEAD"), argsOnly = true)
    private ResourceKey<T> thaumaturge$resolveKey(ResourceKey<T> id) {
        if (id == null || !id.isFor(key)) {
            return id;
        }
        ResourceLocation canonical = thaumaturge$resolveLocation(id.location());
        return canonical.equals(id.location()) ? id : ResourceKey.create(key, canonical);
    }

    @ModifyVariable(method = {
        "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
        "containsKey(Lnet/minecraft/resources/ResourceLocation;)Z"
    }, at = @At("HEAD"), argsOnly = true)
    private ResourceLocation thaumaturge$resolveLocation(ResourceLocation id) {
        ResourceLocation candidate = LegacyRegistryAliases.candidate(key, id);
        return candidate != id && !byLocation.containsKey(id) && byLocation.containsKey(candidate) ? candidate : id;
    }
}
