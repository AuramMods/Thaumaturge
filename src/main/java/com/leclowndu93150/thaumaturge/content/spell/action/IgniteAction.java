package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record IgniteAction(float seconds, float perDuration) implements SpellAction {
    public static final MapCodec<IgniteAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codecs.optionalFieldOf(Codec.FLOAT, "seconds", 1.0F).forGetter(IgniteAction::seconds),
                    Codecs.optionalFieldOf(Codec.FLOAT, "per_duration", 1.0F).forGetter(IgniteAction::perDuration))
            .apply(i, IgniteAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.IGNITE.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        ActionTargets.entity(ctx)
                .filter(entity -> !entity.fireImmune())
                .ifPresent(entity -> entity.igniteForSeconds(ActionTargets.lasting(seconds, perDuration, ctx)));
    }
}
