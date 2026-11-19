package com.leclowndu93150.thaumaturge.mixin.registry;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ForgeRegistry.class, remap = false)
public interface ForgeRegistryAliasAccessor {
    @Accessor("aliases")
    Map<ResourceLocation, ResourceLocation> thaumaturge$aliases();
}
