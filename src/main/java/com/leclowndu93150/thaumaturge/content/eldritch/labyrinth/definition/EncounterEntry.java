package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;

public record EncounterEntry(Holder<LabyrinthEncounter> encounter, int weight, Optional<HolderSet<RoomType>> halls) {
    public static final Codec<EncounterEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    LabyrinthEncounter.CODEC.fieldOf("encounter").forGetter(EncounterEntry::encounter),
                    Codecs.optionalFieldOf(ExtraCodecs.POSITIVE_INT, "weight", 1).forGetter(EncounterEntry::weight),
                    Codecs.optionalFieldOf(RoomType.SET_CODEC, "halls").forGetter(EncounterEntry::halls))
            .apply(instance, EncounterEntry::new));
}
