package com.leclowndu93150.thaumaturge.network.transport;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Loaded only when a physical client encodes/decodes its play connection. */
@OnlyIn(Dist.CLIENT)
final class ClientPayloadState {
    private ClientPayloadState() {}

    static RegistryAccess registries() {
        // The connection's synced registries exist before the client level is constructed.
        return Objects.requireNonNull(Minecraft.getInstance().getConnection(), "No play connection").registryAccess();
    }

    static Player player() {
        return Objects.requireNonNull(Minecraft.getInstance().player, "No client player for gameplay payload");
    }
}
