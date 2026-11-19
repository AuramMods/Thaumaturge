package com.leclowndu93150.thaumaturge.api.aura;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Holder for the vis relay block capabilities.
 *
 * @since 1.0.0
 */
public final class VisRelayCapabilities {
    /**
     * Block capability for a vis relay network source. It has no context: relays query it without
     * a side.
     */
    public static final Capability<IVisRelaySource> SOURCE = CapabilityManager.get(new CapabilityToken<>() {});

    private VisRelayCapabilities() {}
}
