package com.leclowndu93150.thaumaturge.mixin.server;

import com.leclowndu93150.thaumaturge.datamap.DataMapLoader;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.datamap.ResourceDataMaps;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableServerResources.class)
abstract class ServerResourceDataMapsMixin implements ResourceDataMaps {
    @Unique private DataMapLoader thaumaturge$dataMapLoader;
    @Override public DataMapLoader thaumaturge$dataMapLoader() { return thaumaturge$dataMapLoader; }

    @Inject(method = "<init>(Lnet/minecraft/core/RegistryAccess$Frozen;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;I)V", at = @At("RETURN"))
    private void thaumaturge$createDataMapLoader(RegistryAccess.Frozen registries, FeatureFlagSet features,
            Commands.CommandSelection commands, int permissionLevel, CallbackInfo ci) {
        var resources = (ReloadableServerResources) (Object) this;
        thaumaturge$dataMapLoader = new DataMapLoader(registries, resources.getConditionContext(), DataMaps.CATALOG);
    }
    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void thaumaturge$includeDataMaps(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        var listeners = new ArrayList<>(cir.getReturnValue()); listeners.add(thaumaturge$dataMapLoader); cir.setReturnValue(List.copyOf(listeners));
    }
    @Inject(method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V", at = @At("TAIL"))
    private void thaumaturge$applyDataMapsAfterTags(RegistryAccess registries, CallbackInfo ci) {
        thaumaturge$dataMapLoader.apply(registries);
    }
}
