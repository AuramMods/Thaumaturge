package com.leclowndu93150.thaumaturge.mixin.world.entity.monster;

import com.leclowndu93150.thaumaturge.content.entity.WaterConversionTarget;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Consumer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
abstract class ZombieWaterConversionMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraftforge/event/ForgeEventFactory;canLivingConvert(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EntityType;Ljava/util/function/Consumer;)Z"))
    private boolean thaumaturge$useActualConversionTarget(LivingEntity entity, EntityType<? extends LivingEntity> type,
            Consumer<Integer> timer, Operation<Boolean> original) {
        EntityType<? extends LivingEntity> target = entity instanceof WaterConversionTarget custom
                ? custom.waterConversionTarget() : type;
        return original.call(entity, target, timer);
    }
}
