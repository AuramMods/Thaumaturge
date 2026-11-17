package com.leclowndu93150.thaumaturge.serialization;

import net.minecraft.resources.RegistryOps;

/** Implemented on native RegistryOps by the common accessor mixin. */
public interface RegistryOpsLookup {
    RegistryOps.RegistryInfoLookup thaumaturge$registryInfoLookup();
}
