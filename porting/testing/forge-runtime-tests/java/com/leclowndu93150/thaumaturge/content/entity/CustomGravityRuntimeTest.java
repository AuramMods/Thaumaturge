package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class CustomGravityRuntimeTest {
    @Test
    void throwableUsesAuthoredGravityBeforePositionAndDrag() {
        Level level = airLevel();
        var projectile = new WeightedProjectile(level);
        projectile.gravity = 0.01;
        projectile.setDeltaMovement(1.0, 0.2, 0.0);

        projectile.tick();

        assertEquals(0.19, projectile.getY(), 1.0e-10);
        assertEquals(0.19 * (double) 0.99F, projectile.getDeltaMovement().y, 1.0e-10);
        assertEquals(1.0, projectile.getX());

        projectile.gravity = 0.0;
        projectile.setDeltaMovement(1.0, 0.2, 0.0);
        projectile.tick();
        assertEquals(0.39, projectile.getY(), 1.0e-10);
    }

    @Test
    void throwableRetainsVanillaTimingAndHonorsNoGravity() {
        Level level = airLevel();
        var vanilla = new ProbeProjectile(level);
        vanilla.setDeltaMovement(1.0, 0.2, 0.0);
        vanilla.tick();
        assertEquals(0.2, vanilla.getY());
        assertEquals(0.2 * (double) 0.99F - (double) 0.03F, vanilla.getDeltaMovement().y, 1.0e-10);

        var custom = new WeightedProjectile(level);
        custom.gravity = 0.1;
        custom.setNoGravity(true);
        custom.setDeltaMovement(1.0, 0.2, 0.0);
        custom.tick();
        assertEquals(0.2, custom.getY());
        assertEquals(0.2 * (double) 0.99F, custom.getDeltaMovement().y, 1.0e-10);
    }

    @Test
    void itemUsesCustomBrakeInItsNativeMovementAndKeepsVanillaDefaultForOtherItems() {
        Level level = airLevel();
        var special = new BrakeItem(level);
        special.setDeltaMovement(0.0, 0.2, 0.0);
        special.tick();
        assertEquals(0.18, special.getY(), 1.0e-10);

        special.setDeltaMovement(0.0, -0.1, 0.0);
        special.tick();
        assertEquals(0.08, special.getY(), 1.0e-10);

        special.setNoGravity(true);
        special.setDeltaMovement(0.0, 0.2, 0.0);
        special.tick();
        assertEquals(0.28, special.getY(), 1.0e-10);

        var vanilla = new ProbeItem(level);
        vanilla.setDeltaMovement(0.0, 0.2, 0.0);
        vanilla.tick();
        assertEquals(0.16, vanilla.getY(), 1.0e-10);
    }

    private static Level airLevel() {
        var level = mock(Level.class);
        when(level.clip(any(ClipContext.class))).thenReturn(BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO));
        when(level.noCollision(any(Entity.class), any(AABB.class))).thenReturn(true);
        return level;
    }

    private static class ProbeProjectile extends ThrowableProjectile {
        ProbeProjectile(Level level) {
            super(EntityType.SNOWBALL, level);
        }

        @Override protected void defineSynchedData() {}
        @Override public void baseTick() {}
        @Override protected void checkInsideBlocks() {}
    }

    private static final class WeightedProjectile extends ProbeProjectile implements CustomGravity {
        double gravity;

        WeightedProjectile(Level level) {
            super(level);
        }

        @Override public double customGravity() {
            return gravity;
        }
    }

    private static class ProbeItem extends ItemEntity {
        ProbeItem(Level level) {
            super(EntityType.ITEM, level);
            setItem(new ItemStack(Items.DIAMOND));
        }

        @Override public void baseTick() {}
        @Override protected boolean updateInWaterStateAndDoFluidPushing() {
            return false;
        }

        @Override public void move(MoverType type, Vec3 movement) {
            setPos(position().add(movement));
        }
    }

    private static final class BrakeItem extends ProbeItem implements CustomGravity {
        BrakeItem(Level level) {
            super(level);
        }

        @Override public double customGravity() {
            return Math.max(0.0, getDeltaMovement().y) * 0.1;
        }
    }
}
