package com.leclowndu93150.thaumaturge.mixin.network;

import com.leclowndu93150.thaumaturge.network.transport.LoginRegistryCodec;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientboundLoginPacket.class)
abstract class ClientboundLoginPacketRegistryMixin {
    @WrapOperation(method = "write", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/network/FriendlyByteBuf;writeWithCodec(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/lang/Object;)V"))
    private void thaumaturge$encodeRegistryReferences(FriendlyByteBuf buffer, DynamicOps<Tag> builtins,
                                                     Codec<RegistryAccess> codec, Object registries,
                                                     Operation<Void> original) {
        original.call(buffer, LoginRegistryCodec.encodingOps(builtins, (RegistryAccess) registries), codec, registries);
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/network/FriendlyByteBuf;readWithCodec(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;)Ljava/lang/Object;"))
    private static Object thaumaturge$decodeRegistryReferences(FriendlyByteBuf buffer, DynamicOps<Tag> builtins,
                                                               Codec<RegistryAccess> codec, Operation<Object> original) {
        return original.call(buffer, builtins, LoginRegistryCodec.withRegistryReferences(codec));
    }
}
