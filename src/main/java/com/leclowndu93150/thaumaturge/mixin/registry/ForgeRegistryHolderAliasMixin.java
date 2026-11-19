package com.leclowndu93150.thaumaturge.mixin.registry;

import com.leclowndu93150.thaumaturge.content.legacy.LegacyRegistryAliases;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Forge resolves aliases for values through its delegate. Holder lookups need the canonical name as well. */
@Mixin(targets = "net.minecraftforge.registries.NamespacedWrapper")
abstract class ForgeRegistryHolderAliasMixin<T> {
    @Shadow(remap = false) @Final private ForgeRegistry<T> delegate;
    @Shadow(remap = false) private Map<ResourceLocation, Holder.Reference<T>> holdersByName;

    @ModifyVariable(method = "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;",
            at = @At("HEAD"), argsOnly = true)
    private ResourceKey<T> thaumaturge$resolveKey(ResourceKey<T> key) {
        if (!key.isFor(delegate.getRegistryKey())) {
            return key;
        }
        ResourceLocation id = thaumaturge$resolveId(key.location());
        return id.equals(key.location()) ? key : ResourceKey.create(delegate.getRegistryKey(), id);
    }

    @ModifyVariable(method = "getHolder(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;",
            at = @At("HEAD"), argsOnly = true, remap = false)
    private ResourceLocation thaumaturge$resolveId(ResourceLocation id) {
        if (LegacyRegistryAliases.candidate(delegate.getRegistryKey(), id) == id || holdersByName.containsKey(id)) {
            return id;
        }
        T value = delegate.getRaw(id);
        return value == null ? id : delegate.getKey(value);
    }
}
