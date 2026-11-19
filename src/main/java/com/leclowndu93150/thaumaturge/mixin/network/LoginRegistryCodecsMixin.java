package com.leclowndu93150.thaumaturge.mixin.network;

import com.leclowndu93150.thaumaturge.network.transport.LoginRegistryCodec;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistryCodecs.class)
abstract class LoginRegistryCodecsMixin {
    // The Mixin processor does not emit a reference map entry for this synthetic method.
    @WrapOperation(method = {"lambda$networkCodec$1", "m_257110_"}, remap = false, at = @At(value = "NEW", target =
            "(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/MappedRegistry;"))
    private static <E> MappedRegistry<E> thaumaturge$useSharedRegistry(ResourceKey<? extends Registry<E>> key,
                                                                     Lifecycle lifecycle, Operation<MappedRegistry<E>> original) {
        MappedRegistry<E> shared = LoginRegistryCodec.registryBeingDecoded(key);
        return shared == null ? original.call(key, lifecycle) : shared;
    }
}
