package com.leclowndu93150.thaumaturge.compat.sable;

import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinition;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes.PhysicsBlockPropertyType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.RegistryManager;

public final class SableBlockPhysics {
    private SableBlockPhysics() {}

    public static void clearProperties() {
        var registry = RegistryManager.ACTIVE.getRegistry(PhysicsBlockPropertyTypes.REGISTRY_KEY);
        var types = registry.getValues();
        Map<ResourceLocation, Object> defaults = new HashMap<>();
        registry.getEntries().forEach(entry -> defaults.put(entry.getKey().location(), entry.getValue().defaultValue()));

        // The state loader reads the property values directly. The selector is unused.
        var reset = new PhysicsBlockPropertiesDefinition(
                new ExtraCodecs.TagOrElementLocation(new ResourceLocation("minecraft", "air"), false),
                0, defaults, Optional.empty());
        for (var block : BuiltInRegistries.BLOCK) {
            var stateDefinition = block.getStateDefinition();
            for (var state : stateDefinition.getPossibleStates()) {
                var physics = (BlockStateExtension) (Object) state;
                if (hasOverrides(physics, types)) {
                    physics.sable$loadProperties(stateDefinition, reset);
                }
            }
        }
    }

    private static boolean hasOverrides(BlockStateExtension physics, Collection<PhysicsBlockPropertyType<?>> types) {
        for (var type : types) {
            if (!Objects.equals(physics.sable$getProperty(type), type.defaultValue())) {
                return true;
            }
        }
        return false;
    }
}
