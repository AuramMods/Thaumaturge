package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityGeometry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
abstract class EntityTypeGeometryMixin implements EntityGeometry.Access {
    @Unique
    private EntityGeometry thaumaturge$geometry;

    @Override
    public EntityGeometry thaumaturge$getGeometry() {
        return thaumaturge$geometry;
    }

    @Override
    public void thaumaturge$setGeometry(EntityGeometry geometry) {
        thaumaturge$geometry = geometry;
    }

    @Inject(method = "getAABB", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$scaleSpawnClearance(double x, double y, double z, CallbackInfoReturnable<AABB> cir) {
        EntityGeometry geometry = thaumaturge$geometry;
        if (geometry != null && geometry.spawnScale() != 1.0F) {
            var type = (EntityType<?>) (Object) this;
            float halfWidth = geometry.spawnScale() * type.getWidth() / 2.0F;
            float height = geometry.spawnScale() * type.getHeight();
            cir.setReturnValue(new AABB(x - halfWidth, y, z - halfWidth, x + halfWidth, y + height, z + halfWidth));
        }
    }
}
