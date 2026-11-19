package com.leclowndu93150.thaumaturge.content.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class AspectCombinationsRuntimeTest {
    @Test
    void unorderedComponentMatchingUsesBoundKeysAcrossRegistryReloads() {
        var before = new MappedRegistry<IAspect>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Holder<IAspect> oldFirst = register(before, "first", List.of());
        Holder<IAspect> oldSecond = register(before, "second", List.of());
        before.freeze();

        var reloaded = new MappedRegistry<IAspect>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Holder<IAspect> first = register(reloaded, "first", List.of());
        Holder<IAspect> second = register(reloaded, "second", List.of());
        Holder<IAspect> compound = register(reloaded, "compound", List.of(first, second));
        Holder<IAspect> doubled = register(reloaded, "doubled", List.of(first, first));
        var registries = new RegistryAccess.ImmutableRegistryAccess(List.of(reloaded.freeze()));

        assertNotSame(first, oldFirst);
        assertSame(compound, AspectCombinations.result(registries, oldFirst, oldSecond));
        assertSame(compound, AspectCombinations.result(registries, oldSecond, oldFirst));
        assertSame(doubled, AspectCombinations.result(registries, oldFirst, first));
        assertNull(AspectCombinations.result(registries, oldSecond, second));
        assertNull(AspectCombinations.result(registries, Holder.direct(first.value()), second));
    }

    private static Holder<IAspect> register(MappedRegistry<IAspect> registry, String name,
            List<Holder<IAspect>> components) {
        var aspect = mock(IAspect.class);
        when(aspect.components()).thenReturn(components);
        var key = ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("port_aspects", name));
        return registry.register(key, aspect, Lifecycle.stable());
    }
}
