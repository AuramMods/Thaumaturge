package com.leclowndu93150.thaumaturge.content.entity.cult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class CultistFireballRuntimeTest {
    @Test
    void nativeFireballKeepsOwnerLaunchHeightAndMotion() {
        var level = mock(ServerLevel.class);
        var caster = caster(level);
        var fireball = CultistFireballs.create(level, caster, new Vec3(0, 3, 4), 64.25);

        assertSame(EntityType.SMALL_FIREBALL, fireball.getType());
        assertSame(caster, fireball.getOwner());
        assertEquals(new Vec3(4.25, 64.25, -9.5), fireball.position());
        assertEquals(0.06, fireball.getDeltaMovement().y, 1e-9);
        assertEquals(0.08, fireball.getDeltaMovement().z, 1e-9);
        assertEquals(0.1, fireball.getDeltaMovement().length(), 1e-9);
        assertTrue(fireball.hasImpulse);

        var packet = (ClientboundAddEntityPacket) fireball.getAddEntityPacket();
        assertEquals(caster.getId(), packet.getData());
        assertEquals(64.25, packet.getY());
        assertEquals(fireball.yPower, packet.getYa(), 1.0 / 8000.0);
        assertEquals(fireball.zPower, packet.getZa(), 1.0 / 8000.0);

        var loaded = reload(fireball, level);
        assertSame(caster, loaded.getOwner());
        assertEquals(fireball.getDeltaMovement(), loaded.getDeltaMovement());
        assertEquals(fireball.yPower, loaded.yPower);
        assertEquals(fireball.zPower, loaded.zPower);
    }

    @Test
    void savedCultistShotUsesConfigAndNativeMobGriefingAtImpact() throws Exception {
        var level = mock(ServerLevel.class);
        var rules = new GameRules();
        when(level.getGameRules()).thenReturn(rules);
        var caster = caster(level);
        var pos = new BlockPos(6, 65, -9);
        BlockPos above = pos.above();
        when(level.getBlockState(any())).thenReturn(Blocks.STONE.defaultBlockState());
        when(level.getBlockState(above)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.isEmptyBlock(above)).thenReturn(true);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

        var spec = ThaumaturgeServerConfig.SPEC;
        var config = CommentedConfig.inMemory();
        spec.correct(config);
        spec.setConfig(config);
        try {
            ThaumaturgeServerConfig.CULTIST_FIREBALLS_IGNITE_BLOCKS.set(false);
            var reloaded = reload(CultistFireballs.create(level, caster, new Vec3(1, 0, 0), 64.25), level);
            impact(reloaded, hit);
            assertTrue(reloaded.isRemoved());
            verify(level, never()).setBlockAndUpdate(eq(above), any());

            impact(new SmallFireball(level, caster, 1, 0, 0), hit);
            verify(level).setBlockAndUpdate(eq(above), any());

            ThaumaturgeServerConfig.CULTIST_FIREBALLS_IGNITE_BLOCKS.set(true);
            impact(CultistFireballs.create(level, caster, new Vec3(1, 0, 0), 64.25), hit);
            verify(level, times(2)).setBlockAndUpdate(eq(above), any());
            rules.getRule(GameRules.RULE_MOBGRIEFING).set(false, null);
            impact(CultistFireballs.create(level, caster, new Vec3(1, 0, 0), 64.25), hit);
            verify(level, times(2)).setBlockAndUpdate(eq(above), any());
        } finally {
            spec.setConfig(null);
        }
    }

    @Test
    void blockIgnitionSettingKeepsEntityDamageAndFire() throws Exception {
        var level = mock(ServerLevel.class);
        var caster = caster(level);
        var target = mock(Entity.class);
        var sources = mock(DamageSources.class);
        var shot = CultistFireballs.create(level, caster, new Vec3(1, 0, 0), 64.25);
        var damage = new DamageSource(Holder.direct(new DamageType("test_fireball", 0.0F)), shot, caster);
        when(level.damageSources()).thenReturn(sources);
        when(sources.fireball(shot, caster)).thenReturn(damage);

        var spec = ThaumaturgeServerConfig.SPEC;
        var config = CommentedConfig.inMemory();
        spec.correct(config);
        spec.setConfig(config);
        try {
            ThaumaturgeServerConfig.CULTIST_FIREBALLS_IGNITE_BLOCKS.set(false);
            impact(shot, new EntityHitResult(target, shot.position()));
            verify(target).setSecondsOnFire(5);
            verify(target).hurt(damage, 5.0F);
        } finally {
            spec.setConfig(null);
        }
    }

    private static Zombie caster(ServerLevel level) {
        var caster = new Zombie(EntityType.ZOMBIE, level);
        caster.setPos(4.25, 63.0, -9.5);
        when(level.getEntity(caster.getUUID())).thenReturn(caster);
        return caster;
    }

    private static SmallFireball reload(SmallFireball fired, ServerLevel level) {
        var restored = new SmallFireball(EntityType.SMALL_FIREBALL, level);
        restored.load(fired.saveWithoutId(new CompoundTag()));
        return restored;
    }

    private static void impact(SmallFireball fireball, HitResult hit) throws Exception {
        var method = SmallFireball.class.getDeclaredMethod("onHit", HitResult.class);
        method.setAccessible(true);
        method.invoke(fireball, hit);
    }
}
