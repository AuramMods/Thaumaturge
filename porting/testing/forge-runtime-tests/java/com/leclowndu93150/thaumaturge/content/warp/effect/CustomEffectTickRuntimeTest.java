package com.leclowndu93150.thaumaturge.content.warp.effect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.taint.effect.VisExhaustEffect;
import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Actual source effect classes and native tick dispatch, with controlled entity/world dependencies. */
public class CustomEffectTickRuntimeTest {
    private static final UnnaturalHungerEffect HUNGER = new UnnaturalHungerEffect();
    private static final SunScornedEffect SUN = new SunScornedEffect();
    private static final List<MobEffect> MARKERS = List.of(new VisExhaustEffect(), new BlurredVisionEffect(), new DeathGazeEffect(), new WarpWardEffect());

    @BeforeAll
    static void registerSourceClassesInTheRealNativeRegistry() {
        ForgeRegistries.MOB_EFFECTS.register(new ResourceLocation("port_source_effect", "hunger"), HUNGER);
        ForgeRegistries.MOB_EFFECTS.register(new ResourceLocation("port_source_effect", "sun"), SUN);
        for (int i = 0; i < MARKERS.size(); i++) {
            ForgeRegistries.MOB_EFFECTS.register(new ResourceLocation("port_source_effect", "marker_" + i), MARKERS.get(i));
        }
    }

    private void tick(MobEffectInstance effect, LivingEntity entity) {
        effect.tick(entity, () -> fail("Unexpected hidden-effect transition"));
    }

    @Test
    void nativeTickDispatchAppliesHungerUntilItsNormalExpiration() {
        var player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(mock(ServerLevel.class));
        var effect = EffectCodecs.instance(HUNGER, 3, 2);
        assertTrue(effect.tick(player, () -> fail("hidden")));
        assertTrue(effect.tick(player, () -> fail("hidden")));
        assertFalse(effect.tick(player, () -> fail("hidden")));
        verify(player, times(3)).causeFoodExhaustion(0.025F * 3);
        assertEquals(0, effect.getDuration());
        assertFalse(effect.tick(player, () -> fail("hidden")));
        verify(player, times(3)).level();
        verifyNoMoreInteractions(player);
    }

    @Test
    void hungerKeepsItsClientAndNonPlayerGuardsAndInfiniteDuration() {
        var client = mock(Level.class);
        when(client.isClientSide()).thenReturn(true);
        var player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(client);
        var effect = EffectCodecs.instance(HUNGER, -1, 1);
        assertTrue(effect.tick(player, () -> fail("hidden")));
        verify(player, never()).causeFoodExhaustion(anyFloat());
        assertEquals(-1, effect.getDuration());
        var creature = mock(LivingEntity.class);
        when(creature.level()).thenReturn(mock(ServerLevel.class));
        tick(effect, creature);
        verify(creature).level();
        verifyNoMoreInteractions(creature);
    }

    private ServerLevel sunnyLevel(LivingEntity entity, int light, float random, boolean loaded) {
        var level = mock(ServerLevel.class);
        var rng = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(rng);
        when(rng.nextFloat()).thenReturn(random);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(loaded);
        when(level.getMaxLocalRawBrightness(any(BlockPos.class))).thenReturn(light);
        when(level.dimensionType()).thenReturn(mock(DimensionType.class));
        when(level.canSeeSky(any(BlockPos.class))).thenReturn(true);
        when(entity.level()).thenReturn(level);
        return level;
    }

    @Test
    void periodicNativeDispatchBurnsForFourSecondsAtTheOriginalBrightnessThreshold() {
        var entity = mock(LivingEntity.class);
        sunnyLevel(entity, 15, 0.0F, true);
        tick(EffectCodecs.instance(SUN, 39), entity);
        verify(entity, never()).setSecondsOnFire(anyInt());
        tick(EffectCodecs.instance(SUN, 40), entity);
        verify(entity).setSecondsOnFire(4);
        verify(entity, never()).heal(anyFloat());
    }

    @Test
    void infinitePeriodicEffectsUseTheNativeEntityCounter() {
        var entity = mock(LivingEntity.class);
        sunnyLevel(entity, 15, 0.0F, true);
        var effect = EffectCodecs.instance(SUN, -1);
        entity.tickCount = 39;
        tick(effect, entity);
        verify(entity, never()).setSecondsOnFire(anyInt());
        entity.tickCount = 40;
        tick(effect, entity);
        verify(entity).setSecondsOnFire(4);
        assertEquals(-1, effect.getDuration());
    }

    @Test
    void darkAndUnloadedChunkBranchesKeepTheirOriginalHealingAndNoLoadBehavior() {
        var entity = mock(LivingEntity.class);
        var level = sunnyLevel(entity, 15, 0.75F, false);
        when(entity.getX()).thenReturn(-0.5);
        when(entity.getZ()).thenReturn(-16.5);
        tick(EffectCodecs.instance(SUN, 40), entity);
        verify(level).hasChunk(-1, -2);
        verify(level, never()).getMaxLocalRawBrightness(any(BlockPos.class));
        verify(entity).heal(1.0F);
        verify(entity, never()).setSecondsOnFire(anyInt());
    }

    @Test
    void clientSunTicksDoNotReadServerLightingOrApplyWorldActions() {
        var entity = mock(LivingEntity.class);
        var level = mock(Level.class);
        when(level.isClientSide()).thenReturn(true);
        when(entity.level()).thenReturn(level);
        tick(EffectCodecs.instance(SUN, 40), entity);
        verify(entity).level();
        verifyNoMoreInteractions(entity);
    }

    @Test
    void markerEffectsKeepNativeDurationWithoutSchedulingGameplayCallbacks() {
        var entity = mock(LivingEntity.class);
        for (var marker : MARKERS) {
            assertFalse(marker.isDurationEffectTick(40, 0));
            var effect = EffectCodecs.instance(marker, 1);
            assertFalse(effect.tick(entity, () -> fail("hidden")));
            assertEquals(0, effect.getDuration());
        }
        verifyNoInteractions(entity);
    }
}
