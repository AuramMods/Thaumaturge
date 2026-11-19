package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.junit.jupiter.api.Test;

class EldritchCrabEffectsRuntimeTest {
    @Test
    void hardSpawnUsesTheOriginalWeightsAndNativeInfiniteEffectFormat() {
        var difficulty = mock(DifficultyInstance.class);
        when(difficulty.getSpecialMultiplier()).thenReturn(0.7F);
        var random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.06F);
        var expected = List.of(MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SPEED,
                MobEffects.DAMAGE_BOOST, MobEffects.REGENERATION, MobEffects.INVISIBILITY);
        for (int roll = 0; roll < expected.size(); roll++) {
            when(random.nextInt(5)).thenReturn(roll);
            var effect = EldritchCrabEffects.forSpawn(Difficulty.HARD, difficulty, random);
            assertNotNull(effect);
            assertSame(expected.get(roll), effect.getEffect());
            assertTrue(effect.isInfiniteDuration());
            assertEquals(0, effect.getAmplifier());
            var loaded = MobEffectInstance.load(effect.save(new CompoundTag()));
            assertNotNull(loaded);
            assertSame(effect.getEffect(), loaded.getEffect());
            assertTrue(loaded.isInfiniteDuration());
        }
    }

    @Test
    void nonHardSpawnsSkipTheRollAndTheHardThresholdIsExclusive() {
        var difficulty = mock(DifficultyInstance.class);
        var random = mock(RandomSource.class);
        for (var mode : List.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL)) {
            assertNull(EldritchCrabEffects.forSpawn(mode, difficulty, random));
        }
        verifyNoInteractions(difficulty, random);
        when(difficulty.getSpecialMultiplier()).thenReturn(0.5F);
        when(random.nextFloat()).thenReturn(0.05F);
        assertNull(EldritchCrabEffects.forSpawn(Difficulty.HARD, difficulty, random));
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void theCrabRejectsNativePoisonAtAnyAmplifier() {
        assertTrue(EldritchCrabEffects.isImmuneTo(new MobEffectInstance(MobEffects.POISON, 30, 0)));
        assertTrue(EldritchCrabEffects.isImmuneTo(new MobEffectInstance(MobEffects.POISON, 30, 4)));
        assertFalse(EldritchCrabEffects.isImmuneTo(new MobEffectInstance(MobEffects.WITHER, 30, 0)));
    }
}
