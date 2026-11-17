package com.leclowndu93150.thaumaturge.mixin.server;

import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
abstract class ReloadableServerResourcesMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void thaumaturge$bindRecipeRegistries(RegistryAccess.Frozen registries, FeatureFlagSet features,
            Commands.CommandSelection commands, int permissionLevel, CallbackInfo ci) {
        RecipeAccess.of(((ReloadableServerResources) (Object) this).getRecipeManager()).attachRegistries(registries);
    }
}
