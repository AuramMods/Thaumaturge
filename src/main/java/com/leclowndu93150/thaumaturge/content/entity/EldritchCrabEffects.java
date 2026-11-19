package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jspecify.annotations.Nullable;

final class EldritchCrabEffects {
    private static final float HARD_GROUP_EFFECT_CHANCE = 0.1F;

    private EldritchCrabEffects() {}

    static @Nullable MobEffectInstance forSpawn(Difficulty difficulty, DifficultyInstance local, RandomSource random) {
        if (difficulty != Difficulty.HARD
                || random.nextFloat() >= HARD_GROUP_EFFECT_CHANCE * local.getSpecialMultiplier()) {
            return null;
        }
        var effect = switch (random.nextInt(5)) {
            case 0, 1 -> MobEffects.MOVEMENT_SPEED;
            case 2 -> MobEffects.DAMAGE_BOOST;
            case 3 -> MobEffects.REGENERATION;
            default -> MobEffects.INVISIBILITY;
        };
        return EffectCodecs.instance(effect, MobEffectInstance.INFINITE_DURATION);
    }

    static boolean isImmuneTo(MobEffectInstance effect) {
        return effect.getEffect() == MobEffects.POISON;
    }
}
