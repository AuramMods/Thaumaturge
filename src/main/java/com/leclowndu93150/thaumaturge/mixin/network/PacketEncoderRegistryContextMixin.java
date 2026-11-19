package com.leclowndu93150.thaumaturge.mixin.network;

import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Supplies the sending connection's world lookup to entity data codecs on the Netty thread. */
@Mixin(PacketEncoder.class)
abstract class PacketEncoderRegistryContextMixin {
    @WrapOperation(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V"))
    private void thaumaturge$writeEntityDataWithWorld(Packet<?> packet, FriendlyByteBuf buffer, Operation<Void> original,
            @Local(argsOnly = true) ChannelHandlerContext context) {
        if (packet instanceof ClientboundSetEntityDataPacket) {
            Connection connection = context.pipeline().get(Connection.class);
            if (connection != null && connection.getPacketListener() instanceof ServerGamePacketListenerImpl listener) {
                buffer = new RegistryFriendlyByteBuf(buffer, listener.getPlayer().serverLevel().registryAccess());
            }
        }
        original.call(packet, buffer);
    }
}
