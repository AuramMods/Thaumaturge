package com.leclowndu93150.thaumaturge.content.taint.effect;

import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageSources;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import net.minecraft.server.level.ServerLevel;
import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class FluxTaintEffect extends MobEffect {
    private static final int BASE_INTERVAL = 40;
    private static final int MAX_AMP_DIVIDER_SHIFT = 5;
    private static final float DAMAGE = 1.0F;
    private static final float HEAL = 1.0F;

    public FluxTaintEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0080);
    }

    @Override
    public boolean isDurationEffectTick(int tickCount, int amplification) {
        int divider = Math.max(1, BASE_INTERVAL >> Math.min(amplification, MAX_AMP_DIVIDER_SHIFT));
        return tickCount % divider == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity mob, int amplification) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        if (MobTraits.isTainted(mob)) {
            mob.heal(HEAL);
            return;
        }
        if (!mob.getType().is(TTEntityTags.UNDEAD)) {
            mob.hurt(TTDamageSources.taint(level), DAMAGE);
        }
        return;
    }
}
