package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Registry-style holder for the aspect capability types.
 *
 * <p>{@link #CONTAINER} is the block capability for {@link IAspectContainer}. Directional
 * consumers supply a side, while infusion source discovery deliberately queries with a
 * {@code null} context. Providers implementing {@link IAspectSource} for infusion must support
 * that null-side query.
 *
 * @since 1.0.0
 */
public final class AspectCapabilities {
    /** Sided block capability for aspect storing. */
    public static final Capability<IAspectContainer> CONTAINER = CapabilityManager.get(new CapabilityToken<>() {});

    private AspectCapabilities() {}
}
