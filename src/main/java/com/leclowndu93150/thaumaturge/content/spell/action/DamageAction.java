package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageSources;
import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record DamageAction(
        ResourceKey<DamageType> damageType,
        float base,
        float perPower,
        float undeadMultiplier,
        boolean waterSensitiveOnly)
        implements SpellAction {
    public static final MapCodec<DamageAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    ResourceKey.codec(Registries.DAMAGE_TYPE)
                            .fieldOf("damage_type")
                            .forGetter(DamageAction::damageType),
                    Codecs.optionalFieldOf(Codec.FLOAT, "base", 0.0F).forGetter(DamageAction::base),
                    Codecs.optionalFieldOf(Codec.FLOAT, "per_power", 1.0F).forGetter(DamageAction::perPower),
                    Codecs.optionalFieldOf(Codec.FLOAT, "undead_multiplier", 1.0F).forGetter(DamageAction::undeadMultiplier),
                    Codecs.optionalFieldOf(Codec.BOOL, "water_sensitive_only", false)
                            .forGetter(DamageAction::waterSensitiveOnly))
            .apply(i, DamageAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.DAMAGE.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        Entity entity = ActionTargets.entity(ctx).orElse(null);
        if (entity == null) {
            return;
        }
        boolean undead = entity.getType().is(TTEntityTags.UNDEAD);
        if (waterSensitiveOnly && !(entity instanceof LivingEntity living && living.isSensitiveToWater())) {
            return;
        }
        float amount = ActionTargets.magnitude(base, perPower, ctx) * (undead ? undeadMultiplier : 1.0F);
        if (amount > 0.0F) {
            entity.hurt(
                    TTDamageSources.source(ctx.cast().level(), damageType, ctx.cast().caster()),
                    amount);
        }
    }
}
