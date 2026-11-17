package com.leclowndu93150.thaumaturge.mixin.resources;

import com.leclowndu93150.thaumaturge.serialization.RegistryOpsLookup;
import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccessor extends RegistryOpsLookup {
    @Override @Accessor("lookupProvider")
    RegistryOps.RegistryInfoLookup thaumaturge$registryInfoLookup();
}
