package com.leclowndu93150.thaumaturge.mixin.client.multiplayer;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
abstract class RemoteLoginReadMixin {
    @Shadow private Channel channel;
    @Shadow @Final private PacketFlow receiving;

    @Inject(method = "setProtocol", at = @At("TAIL"))
    private void thaumaturge$rearmLoginReads(ConnectionProtocol protocol, CallbackInfo callback) {
        if (protocol != ConnectionProtocol.LOGIN || receiving != PacketFlow.CLIENTBOUND
                || !(channel instanceof SocketChannel socket)) {
            return;
        }

        // A queued read clear can outlast the initial protocol's enable. Forge's login
        // enable then sees auto-read already set and skips rearming the socket.
        socket.eventLoop().execute(() -> {
            if (socket.isActive() && socket.config().isAutoRead()) {
                socket.read();
            }
        });
    }
}
