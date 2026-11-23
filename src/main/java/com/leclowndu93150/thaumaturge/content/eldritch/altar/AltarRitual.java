package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;

record AltarRitual(UUID caster, long start, float drained, int steps, Optional<Terms> terms,
                   Optional<MazeId> opened) {
    public static final Codec<AltarRitual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("caster").forGetter(AltarRitual::caster),
                    Codec.LONG.fieldOf("start").forGetter(AltarRitual::start),
                    Codec.FLOAT.fieldOf("drained").forGetter(AltarRitual::drained),
                    Codecs.optionalFieldOf(Codec.INT, "steps", 0).forGetter(AltarRitual::steps),
                    Codecs.optionalFieldOf(Terms.CODEC, "terms").forGetter(AltarRitual::terms),
                    Codecs.optionalFieldOf(MazeId.CODEC, "opened").forGetter(AltarRitual::opened))
            .apply(instance, AltarRitual::new));

    AltarRitual withTerms(Terms agreed, int completedSteps) {
        return new AltarRitual(caster, start, drained, completedSteps, Optional.of(agreed), opened);
    }

    AltarRitual drain(float amount) {
        return new AltarRitual(caster, start, drained + amount, steps + 1, terms, opened);
    }

    AltarRitual withOpened(MazeId record) {
        return new AltarRitual(caster, start, drained, steps, terms, Optional.of(record));
    }

    record Terms(ResourceKey<LabyrinthDefinition> definition, StageAuthorization authority, int eyes, int duration,
                 float vis, boolean exactPayment) {
        static final Codec<Terms> CODEC = RecordCodecBuilder.create(i -> i.group(
                        ResourceKey.codec(LabyrinthDefinition.REGISTRY_KEY).fieldOf("definition")
                                .forGetter(Terms::definition),
                        StageAuthorization.CODEC.fieldOf("authority").forGetter(Terms::authority),
                        Codec.intRange(1, BlockEntityEldritchAltar.MAX_EYES).fieldOf("eyes").forGetter(Terms::eyes),
                        Codec.intRange(1, 1200).fieldOf("duration").forGetter(Terms::duration),
                        Codec.floatRange(0.0F, 1000.0F).fieldOf("vis").forGetter(Terms::vis),
                        Codec.BOOL.fieldOf("exact_payment").forGetter(Terms::exactPayment))
                .apply(i, Terms::new));
    }
}
