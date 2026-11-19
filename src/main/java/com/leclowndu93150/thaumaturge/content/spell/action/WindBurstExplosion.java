package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jspecify.annotations.Nullable;

/** Uses Forge's blast tracing and event hooks for a non-destructive Aer burst. */
public final class WindBurstExplosion extends Explosion {
    private static final double VIEW_RANGE_SQUARED = 64.0 * 64.0;
    private static final Optional<Float> WIND_BARRIER_RESISTANCE = Optional.of(3_600_000.0F);
    private static final ExplosionDamageCalculator WIND_RESISTANCE = new ExplosionDamageCalculator() {
        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level,
                BlockPos pos, BlockState block, FluidState fluid) {
            return block.is(TTBlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS)
                    ? WIND_BARRIER_RESISTANCE : Optional.empty();
        }
    };

    private final ServerLevel level;
    private final @Nullable LivingEntity caster;
    private final float radius;

    WindBurstExplosion(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, float radius) {
        // No source entity is excluded from the blast, so the caster can be launched too.
        super(level, null, level.damageSources().explosion(null, caster), WIND_RESISTANCE,
                center.x, center.y, center.z, radius, false, BlockInteraction.KEEP);
        this.level = level;
        this.caster = caster;
        this.radius = radius;
    }

    @Override
    public @Nullable LivingEntity getIndirectSourceEntity() {
        return caster;
    }

    @Override
    public boolean interactsWithBlocks() {
        return true;
    }

    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        var struckBlocks = new ObjectArrayList<>(getToBlow());
        Util.shuffle(struckBlocks, level.getRandom());
        for (BlockPos pos : struckBlocks) {
            WindBurstTriggers.trigger(level, pos);
        }

        Vec3 center = getPosition();
        level.playSound(null, center.x, center.y, center.z, TTSounds.WIND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        boolean large = radius >= 2.0F;
        var gust = new AirGustParticleOptions(large ? 2.5F : 1.5F);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center) >= VIEW_RANGE_SQUARED) {
                continue;
            }
            if (spawnParticles) {
                level.sendParticles(player, gust, true, center.x, center.y, center.z,
                        large ? 8 : 4, 0.35, 0.35, 0.35, 0.0);
            }
            if (getHitPlayers().containsKey(player)) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
        }
    }

    public static void burst(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, float radius) {
        var wind = new WindBurstExplosion(level, caster, center, radius);
        if (!ForgeEventFactory.onExplosionStart(level, wind)) {
            wind.explode();
            wind.finalizeExplosion(true);
        }
    }
}
