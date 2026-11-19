package com.leclowndu93150.thaumaturge.content.entity.projectile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.mixin.world.entity.EntityRandomAccessor;
import java.util.ArrayList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ChargeTrailRuntimeTest {
    @Test
    void theMappedAccessorReturnsEachProjectilesOwnNativeStream() {
        var level = mock(Level.class);
        var first = new ProbeProjectile(level);
        var second = new ProbeProjectile(level);
        assertSame(first.nativeRandom(), ((EntityRandomAccessor) (Snowball) first).thaumaturge$getRandom());
        assertSame(second.nativeRandom(), ((EntityRandomAccessor) (Snowball) second).thaumaturge$getRandom());
        assertNotSame(first.nativeRandom(), second.nativeRandom());
        verify(level, never()).getRandom();
    }

    @Test
    void emittingAChargeTrailConsumesOnlyTheProjectileStreamAndKeepsTheAuthoredParticles() {
        var level = mock(Level.class);
        var projectile = new ProbeProjectile(level);
        projectile.xo = 1.0;
        projectile.yo = 2.0;
        projectile.zo = 3.0;
        projectile.setPos(5.0, 6.0, 7.0);
        double lift = projectile.getBbHeight() * 0.5;
        long seed = 0x36B4AFL;
        projectile.nativeRandom().setSeed(seed);
        var emitted = new ArrayList<Emission>();
        doAnswer(call -> {
            emitted.add(new Emission(call.getArgument(0),
                    new Vec3(call.getArgument(1), call.getArgument(2), call.getArgument(3)),
                    new Vec3(call.getArgument(4), call.getArgument(5), call.getArgument(6))));
            return null;
        }).when(level).addParticle(any(ParticleOptions.class), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble());

        new ChargeTrail(0x9A6AAE, 0.3F, 0.55F, 3.0F).emit(projectile);

        assertEquals(5, emitted.size());
        var expectedStream = RandomSource.create(seed);
        var from = new Vec3(1.0, 2.0, 3.0);
        var to = new Vec3(5.0, 6.0, 7.0);
        for (int i = 0; i < 4; i++) {
            var puff = emitted.get(i);
            var mote = assertInstanceOf(FireMoteParticleOptions.class, puff.particle());
            assertEquals(from.lerp(to, expectedStream.nextDouble()).add(0, lift, 0), puff.position());
            float tone = 1.0F - expectedStream.nextFloat() * 0.3F;
            assertEquals(0x9A / 255.0F * tone, mote.r());
            assertEquals(0x6A / 255.0F * tone, mote.g());
            assertEquals(0xAE / 255.0F * tone, mote.b());
            assertEquals(expectedStream.triangle(0, 0.008), mote.vx());
            assertEquals(expectedStream.triangle(0, 0.008), mote.vy());
            assertEquals(expectedStream.triangle(0, 0.008), mote.vz());
            assertEquals(0.55F, mote.alpha());
            assertEquals(3.0F, mote.scale());
            assertTrue(mote.translucent());
            assertEquals(Vec3.ZERO, puff.velocity());
        }

        var spark = emitted.get(4);
        assertEquals(new ShieldSparkParticleOptions(0xFFF4E8, 0.8F, 0.25F, 6, 0, false), spark.particle());
        assertEquals(new Vec3(to.x + expectedStream.triangle(0, 0.15),
                to.y + lift + expectedStream.triangle(0, 0.15),
                to.z + expectedStream.triangle(0, 0.15)), spark.position());
        assertEquals(Vec3.ZERO, spark.velocity());
        assertEquals(expectedStream.nextLong(), projectile.nativeRandom().nextLong());
        verify(level, never()).getRandom();
    }

    private record Emission(ParticleOptions particle, Vec3 position, Vec3 velocity) {}

    private static final class ProbeProjectile extends Snowball {
        ProbeProjectile(Level level) { super(EntityType.SNOWBALL, level); }

        RandomSource nativeRandom() { return random; }
    }
}
