package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;

public final class AspectCombinations {

    private AspectCombinations() {}

    public static @Nullable Holder<IAspect> result(
            HolderLookup.Provider registries, Holder<IAspect> first, Holder<IAspect> second) {
        var firstKey = first.unwrapKey().orElse(null);
        var secondKey = second.unwrapKey().orElse(null);
        if (firstKey == null || secondKey == null) {
            return null;
        }
        for (Holder.Reference<IAspect> candidate :
                registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().toList()) {
            List<Holder<IAspect>> components = candidate.value().components();
            if (components.size() != 2) {
                continue;
            }
            boolean direct = components.get(0).is(firstKey) && components.get(1).is(secondKey);
            boolean swapped = components.get(0).is(secondKey) && components.get(1).is(firstKey);
            if (direct || swapped) {
                return candidate;
            }
        }
        return null;
    }
}
