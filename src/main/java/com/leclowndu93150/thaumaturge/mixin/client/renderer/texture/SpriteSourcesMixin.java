package com.leclowndu93150.thaumaturge.mixin.client.renderer.texture;

import com.google.common.collect.BiMap;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.equipment.OptionalPalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteSources.class)
public abstract class SpriteSourcesMixin {
    @Shadow
    @Final
    private static BiMap<ResourceLocation, SpriteSourceType> TYPES;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void thaumaturge$registerOptionalPermutations(CallbackInfo ci) {
        TYPES.put(TTIds.rl("optional_paletted_permutations"), OptionalPalettedPermutations.TYPE);
    }
}
