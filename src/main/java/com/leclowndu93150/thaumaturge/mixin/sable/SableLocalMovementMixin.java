package com.leclowndu93150.thaumaturge.mixin.sable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.network.packets.ClientboundSableSnapshotDualPacket;
import dev.ryanhcode.sable.network.packets.ClientboundSableSnapshotInfoDualPacket;
import dev.ryanhcode.sable.network.tcp.SablePacketManager;
import dev.ryanhcode.sable.network.tcp.SableTCPPacket;
import dev.ryanhcode.sable.network.udp.SableUDPPacket;
import io.netty.channel.local.LocalAddress;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.network.udp.SableUDPServer", remap = false)
abstract class SableLocalMovementMixin {
    @WrapMethod(method = "sendUDPPacket")
    private boolean thaumaturge$orderLocalMovement(ServerPlayer player, SableUDPPacket packet, boolean flush,
                                                   Operation<Boolean> original) {
        if (player.connection.getRemoteAddress() instanceof LocalAddress
                && (packet instanceof ClientboundSableSnapshotDualPacket
                || packet instanceof ClientboundSableSnapshotInfoDualPacket)) {
            // Local play packets share the ordered connection that creates the sub-level.
            player.connection.send(SablePacketManager.clientboundVanillaPacket((SableTCPPacket) packet));
            return true;
        }
        return original.call(player, packet, flush);
    }
}
