package com.leclowndu93150.thaumaturge.testing;

import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

public final class AspectRegistryFixture {
    private AspectRegistryFixture() {}

    @SafeVarargs
    public static RegistryAccess.Frozen create(ResourceKey<IAspect>... keys) {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        for (ResourceKey<IAspect> key : keys) {
            Registry.register(registry, key, mock(IAspect.class));
        }
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
    }
}
