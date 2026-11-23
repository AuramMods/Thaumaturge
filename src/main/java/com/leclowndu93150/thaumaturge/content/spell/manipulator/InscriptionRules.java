package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record InscriptionRules(boolean enabled, float levels, float crystals, float vis, int interval, float drain) {
    static final int DEFAULT_INTERVAL = 20;
    static final float DEFAULT_DRAIN = 20.0F;
    private static final InscriptionRules DEFAULT =
            new InscriptionRules(true, 1.0F, 1.0F, 1.0F, DEFAULT_INTERVAL, DEFAULT_DRAIN);
    private static final Codec<Float> MULTIPLIER = Codec.floatRange(0.0F, 16.0F);
    private static final Codec<InscriptionRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(InscriptionRules::enabled),
                    Codecs.optionalFieldOf(MULTIPLIER, "levels_multiplier", 1.0F).forGetter(InscriptionRules::levels),
                    Codecs.optionalFieldOf(MULTIPLIER, "crystals_multiplier", 1.0F).forGetter(InscriptionRules::crystals),
                    Codecs.optionalFieldOf(Codec.floatRange(0.01F, 16.0F), "vis_multiplier", 1.0F)
                            .forGetter(InscriptionRules::vis),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1200), "interval", DEFAULT_INTERVAL)
                            .forGetter(InscriptionRules::interval),
                    Codecs.optionalFieldOf(Codec.floatRange(0.01F, 1000.0F), "drain", DEFAULT_DRAIN)
                            .forGetter(InscriptionRules::drain))
            .apply(i, InscriptionRules::new));

    public static final DataMapType<Item, InscriptionRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("focus_inscription"), Registries.ITEM, CODEC).synced(CODEC, false).build();

    public static InscriptionRules of(ItemStack focus) {
        var rule = DataMaps.get(focus.getItemHolder(), DATA_MAP);
        return rule == null ? DEFAULT : rule;
    }

    public SpellSummary adjusted(SpellSummary base) {
        if (levels == 1.0F && crystals == 1.0F) return base;
        Map<ResourceKey<IAspect>, Integer> costs = new LinkedHashMap<>();
        base.crystals().forEach((aspect, amount) -> {
            int scaled = Math.round(amount * crystals);
            if (scaled > 0) costs.put(aspect, scaled);
        });
        return new SpellSummary(base.complexity(), base.budget(), base.vis(), Math.round(base.xp() * levels), costs,
                base.depth(), base.branches(), base.repeats(), base.cooldown(), base.chargeTicks(), base.pulseInterval(),
                base.problems());
    }
}
