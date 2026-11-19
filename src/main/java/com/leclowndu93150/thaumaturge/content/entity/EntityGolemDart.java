package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class EntityGolemDart extends AbstractArrow {
    public EntityGolemDart(EntityType<? extends EntityGolemDart> type, Level level) {
        super(type, level);
    }

    private EntityGolemDart(EntityType<EntityGolemDart> type, LivingEntity shooter) {
        super(type, shooter, shooter.level());
        pickup = Pickup.DISALLOWED;
    }

    public static void loose(
            EntityType<EntityGolemDart> type,
            LivingEntity shooter,
            LivingEntity target,
            double damage,
            double loft,
            float velocity,
            float inaccuracy) {
        EntityGolemDart dart = new EntityGolemDart(type, shooter);
        dart.setBaseDamage(damage);
        Vec3 aim = aimVector(dart.position(), new Vec3(target.getX(), target.getEyeY(), target.getZ()), loft);
        dart.shoot(aim.x, aim.y, aim.z, velocity, inaccuracy);
        shooter.level().addFreshEntity(dart);
    }

    static Vec3 aimVector(Vec3 projectilePosition, Vec3 targetPosition, double loft) {
        return targetPosition.add(0.0, loft, 0.0).subtract(projectilePosition);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
