package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class GolemStages {
    private GolemStages() {}

    public static StageTarget design(ResourceLocation family, IGolemProperties build) {
        var material = TTGolemParts.materials().getResourceKey(build.material());
        StageTarget target = material.map(key -> StageTarget.recipe(family, key.location()).withSubject(key))
                .orElseGet(() -> StageTarget.family(family));
        target = withPart(target, TTGolemParts.heads(), build.head());
        target = withPart(target, TTGolemParts.arms(), build.arms());
        target = withPart(target, TTGolemParts.legs(), build.legs());
        target = withPart(target, TTGolemParts.addons(), build.addon());
        for (var trait : build.traits()) {
            target = withPart(target, TTGolemTraits.registry(), trait);
        }
        return target;
    }

    private static <T> StageTarget withPart(StageTarget target, Registry<T> registry, T entry) {
        return registry.getResourceKey(entry).map(target::withSubject).orElse(target);
    }

    public static StageTarget seal(ResourceLocation id) {
        return StageTarget.recipe(StageTarget.GOLEM_SEAL, id)
                .withSubject(ResourceKey.create(SealType.REGISTRY_KEY, id));
    }
}
