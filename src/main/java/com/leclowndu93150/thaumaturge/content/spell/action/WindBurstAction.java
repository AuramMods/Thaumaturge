package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WindBurstAction(float base, float perPower, float max) implements SpellAction {
    public static final MapCodec<WindBurstAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codecs.optionalFieldOf(Codec.FLOAT, "base", 1.0F).forGetter(WindBurstAction::base),
                    Codecs.optionalFieldOf(Codec.FLOAT, "per_power", 0.4F).forGetter(WindBurstAction::perPower),
                    Codecs.optionalFieldOf(Codec.FLOAT, "max", 3.0F).forGetter(WindBurstAction::max))
            .apply(i, WindBurstAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.WIND_BURST.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        float radius = Math.min(max, ActionTargets.magnitude(base, perPower, ctx) + ctx.radius());
        WindBurstExplosion.burst(ctx.cast().level(), ctx.cast().caster(), ctx.target().position(), radius);
    }
}
