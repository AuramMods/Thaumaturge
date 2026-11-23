package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public record StabilizerRules(boolean enabled, int chargeInterval, float fluxPerCharge, int pulseInterval,
                              int pulseCost, int reach) {
    static final StabilizerRules DEFAULT = new StabilizerRules(true, 20, 0.25F, 5, 1, 8);
    private static final Codec<StabilizerRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(StabilizerRules::enabled),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1200), "charge_interval", DEFAULT.chargeInterval())
                            .forGetter(StabilizerRules::chargeInterval),
                    Codecs.optionalFieldOf(Codec.floatRange(0.0F, 100.0F), "flux_per_charge", DEFAULT.fluxPerCharge())
                            .forGetter(StabilizerRules::fluxPerCharge),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1200), "pulse_interval", DEFAULT.pulseInterval())
                            .forGetter(StabilizerRules::pulseInterval),
                    Codecs.optionalFieldOf(Codec.intRange(1, BlockEntityStabilizer.MAX_CHARGE), "pulse_cost",
                            DEFAULT.pulseCost()).forGetter(StabilizerRules::pulseCost),
                    Codecs.optionalFieldOf(Codec.intRange(1, 32), "reach", DEFAULT.reach())
                            .forGetter(StabilizerRules::reach))
            .apply(i, StabilizerRules::new));

    public static final DataMapType<Block, StabilizerRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("stabilizer"), Registries.BLOCK, CODEC).build();
}
