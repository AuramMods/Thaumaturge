package com.leclowndu93150.thaumaturge.content.entity.cult;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public final class CultistFireballs {
    private static final String CULTIST_SHOT = "thaumaturge:cultist_fireball";

    private CultistFireballs() {}

    static SmallFireball create(ServerLevel level, Mob caster, Vec3 heading, double spawnY) {
        Vec3 direction = heading.normalize();
        var fireball = new SmallFireball(level, caster, direction.x, direction.y, direction.z);
        // The source release also starts moving at its acceleration before the first tick.
        fireball.setDeltaMovement(fireball.xPower, fireball.yPower, fireball.zPower);
        fireball.hasImpulse = true;
        fireball.setPos(fireball.getX(), spawnY, fireball.getZ());
        fireball.getPersistentData().putBoolean(CULTIST_SHOT, true);
        return fireball;
    }

    public static boolean blocksImpact(SmallFireball fireball) {
        return fireball.getPersistentData().getBoolean(CULTIST_SHOT)
                && !ThaumaturgeServerConfig.CULTIST_FIREBALLS_IGNITE_BLOCKS.get();
    }
}
