package com.leclowndu93150.thaumaturge.mixin.network;

import com.leclowndu93150.thaumaturge.network.transport.LoginRegistryCodec;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Connection.class)
abstract class ConnectionRegistryContextMixin {
    @Unique private RegistryAccess.Frozen thaumaturge$receivedRegistries;

    @WrapOperation(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"))
    private void thaumaturge$dispatchWithConnectionRegistries(Packet<?> packet, PacketListener listener,
                                                               Operation<Void> original) {
        if (packet instanceof ClientboundLoginPacket login) {
            // This also runs on the integrated server's local transport, which has no packet decoder.
            thaumaturge$receivedRegistries = LoginRegistryCodec.includeBuiltins(login.registryHolder());
        }
        if (packet instanceof ClientboundCustomPayloadPacket && thaumaturge$receivedRegistries != null) {
            RegistryContext.withRegistries(thaumaturge$receivedRegistries, () -> original.call(packet, listener));
        } else {
            original.call(packet, listener);
        }
    }
}
