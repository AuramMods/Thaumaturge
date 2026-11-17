package com.leclowndu93150.thaumaturge.mixin.network;

import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Login can be decoded on Netty before the main thread creates its client world. */
@Mixin(PacketDecoder.class)
abstract class PacketDecoderRegistryContextMixin {
    @Unique private RegistryAccess.Frozen thaumaturge$connectionRegistries;

    @Redirect(method = "decode", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/network/ConnectionProtocol;createPacket(Lnet/minecraft/network/protocol/PacketFlow;ILnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/Packet;"))
    private Packet<?> thaumaturge$decodeWithConnectionRegistries(ConnectionProtocol protocol, PacketFlow flow,
                                                               int id, FriendlyByteBuf source) {
        FriendlyByteBuf buffer = thaumaturge$connectionRegistries == null ? source
                : new RegistryFriendlyByteBuf(source, thaumaturge$connectionRegistries);
        return protocol.createPacket(flow, id, buffer);
    }

    // This runs after native size/packet validation, at the original delivery point.
    @Redirect(method = "decode", at = @At(value = "INVOKE", target =
            "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private boolean thaumaturge$rememberValidatedLogin(List<Object> output, Object packet) {
        if (packet instanceof ClientboundLoginPacket login) {
            Map<ResourceKey<?>, Registry<?>> registries = new LinkedHashMap<>();
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries()
                    .forEach(entry -> registries.put(entry.key(), entry.value()));
            login.registryHolder().registries().forEach(entry -> registries.put(entry.key(), entry.value()));
            // Keep the received Registry/Holder instances and numeric IDs; only the view is composed.
            thaumaturge$connectionRegistries = new RegistryAccess.ImmutableRegistryAccess(List.copyOf(registries.values())).freeze();
        }
        return output.add(packet);
    }
}
