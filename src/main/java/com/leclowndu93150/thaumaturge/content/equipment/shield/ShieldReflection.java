package com.leclowndu93150.thaumaturge.content.equipment.shield;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class ShieldReflection {
    private static final float HALF_TURN = 180.0F;
    private static final double REFLECTED_ACCELERATION = 0.5;
    private static final int REFLECT_SHIELD_DAMAGE = 3;
    private static final float BLOCK_SOUND_VOLUME = 1.0F;
    private static final float BLOCK_SOUND_PITCH = 0.8F;
    private static final float BLOCK_SOUND_PITCH_SPREAD = 0.4F;

    private ShieldReflection() {}

    static ItemStack blockingItem(LivingEntity blocker) {
        return blocker.isBlocking() ? blocker.getUseItem() : ItemStack.EMPTY;
    }

    static boolean facing(LivingEntity blocker, Vec3 target) {
        Vec3 view = Vec3.directionFromRotation(0.0F, blocker.getYHeadRot());
        Vec3 toward = target.subtract(blocker.position());
        return new Vec3(toward.x, 0.0, toward.z).dot(view) > 0.0;
    }

    static void reflect(Projectile projectile, LivingEntity blocker, ItemStack shield) {
        if (blocker.level().isClientSide()) {
            return;
        }
        Vec3 reflectedMotion = projectile.getDeltaMovement().reverse();
        projectile.setDeltaMovement(reflectedMotion);
        projectile.setYRot(projectile.getYRot() + HALF_TURN);
        projectile.yRotO += HALF_TURN;
        projectile.hasImpulse = true;
        transferOwner(projectile, blocker);
        if (projectile instanceof AbstractHurtingProjectile accelerating) {
            double acceleration = new Vec3(accelerating.xPower, accelerating.yPower, accelerating.zPower).length();
            Vec3 reflectedPower = reflectedMotion.normalize().scale(acceleration * REFLECTED_ACCELERATION);
            accelerating.xPower = reflectedPower.x;
            accelerating.yPower = reflectedPower.y;
            accelerating.zPower = reflectedPower.z;
        }
        blocker.level()
                .playSound(
                        null,
                        blocker.getX(),
                        blocker.getY(),
                        blocker.getZ(),
                        SoundEvents.SHIELD_BLOCK,
                        blocker.getSoundSource(),
                        BLOCK_SOUND_VOLUME,
                        BLOCK_SOUND_PITCH + blocker.getRandom().nextFloat() * BLOCK_SOUND_PITCH_SPREAD);
        var hand = blocker.getUsedItemHand();
        shield.hurtAndBreak(REFLECT_SHIELD_DAMAGE, blocker, wearer -> wearer.broadcastBreakEvent(hand));
    }

    private static void transferOwner(Projectile projectile, LivingEntity blocker) {
        if (projectile instanceof AbstractArrow arrow) {
            var pickup = arrow.pickup;
            arrow.setOwner(blocker);
            // The newer arrow only promotes a previously disallowed pickup when a player takes ownership.
            arrow.pickup = pickup == AbstractArrow.Pickup.DISALLOWED && blocker instanceof Player
                    ? AbstractArrow.Pickup.ALLOWED : pickup;
        } else {
            projectile.setOwner(blocker);
        }
    }
}
