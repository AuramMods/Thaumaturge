package com.leclowndu93150.thaumaturge.mixin.world.level.loot;

import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootContextParamSets.class)
abstract class LootContextParamSetsMixin {
    @Shadow
    private static LootContextParamSet register(String id, Consumer<LootContextParamSet.Builder> configure) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void thaumaturge$registerEquipmentParams(CallbackInfo ci) {
        register("equipment", builder -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
    }
}
