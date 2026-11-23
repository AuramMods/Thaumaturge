package com.leclowndu93150.thaumaturge.stage;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** The identity of a server operation, including the content selected for it. */
public record StageTarget(ResourceLocation family, Optional<ResourceLocation> id, List<Subject> subjects) {
    public static final ResourceLocation ARCANE_WORKBENCH = TTIds.rl("arcane_workbench");
    public static final ResourceLocation CRUCIBLE = TTIds.rl("crucible");
    public static final ResourceLocation THAUMATORIUM = TTIds.rl("thaumatorium");
    public static final ResourceLocation INFUSION = TTIds.rl("infusion");
    public static final ResourceLocation DUST_TRIGGER = TTIds.rl("dust_trigger");

    public static final Codec<StageTarget> CODEC = RecordCodecBuilder.create(i -> i.group(
                    ResourceLocation.CODEC.fieldOf("family").forGetter(StageTarget::family),
                    Codecs.optionalFieldOf(ResourceLocation.CODEC, "id").forGetter(StageTarget::id),
                    Codecs.optionalFieldOf(Subject.CODEC.listOf(), "subjects", List.of())
                            .forGetter(StageTarget::subjects))
            .apply(i, StageTarget::new));

    public StageTarget {
        subjects = List.copyOf(subjects);
    }

    public static StageTarget family(ResourceLocation family) {
        return new StageTarget(family, Optional.empty(), List.of());
    }

    public static StageTarget recipe(ResourceLocation family, ResourceLocation id) {
        return new StageTarget(family, Optional.of(id), List.of());
    }

    public StageTarget withSubject(ResourceKey<?> key) {
        List<Subject> entries = new ArrayList<>(subjects);
        entries.add(new Subject(key.registry(), key.location()));
        return new StageTarget(family, id, entries);
    }

    public record Subject(ResourceLocation registry, ResourceLocation id) {
        static final Codec<Subject> CODEC = RecordCodecBuilder.create(i -> i.group(
                        ResourceLocation.CODEC.fieldOf("registry").forGetter(Subject::registry),
                        ResourceLocation.CODEC.fieldOf("id").forGetter(Subject::id))
                .apply(i, Subject::new));
    }
}
