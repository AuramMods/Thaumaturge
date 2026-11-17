package com.leclowndu93150.thaumaturge.mixin.server.level;

import com.leclowndu93150.thaumaturge.event.tick.EntityTickHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
abstract class ServerLevelEntityTickMixin {
    @Redirect(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void thaumaturge$entityTick(Entity entity) {
        EntityTickHooks.tick(entity);
    }
}
