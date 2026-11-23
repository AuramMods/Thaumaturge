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
    public static final ResourceLocation RESEARCH = TTIds.rl("research");
    public static final ResourceLocation SCAN = TTIds.rl("scan");
    public static final ResourceLocation ESSENTIA_CRYSTALIZER = TTIds.rl("essentia_crystalizer");
    public static final ResourceLocation SPA = TTIds.rl("spa");
    public static final ResourceLocation PATTERN_CRAFTER = TTIds.rl("pattern_crafter");
    public static final ResourceLocation INFERNAL_FURNACE = TTIds.rl("infernal_furnace");
    public static final ResourceLocation ALCHEMICAL_FURNACE = TTIds.rl("alchemical_furnace");
    public static final ResourceLocation ADVANCED_ALCHEMICAL_FURNACE = TTIds.rl("advanced_alchemical_furnace");
    public static final ResourceLocation DECONSTRUCTION_TABLE = TTIds.rl("deconstruction_table");
    public static final ResourceLocation GOLEM_ASSEMBLY = TTIds.rl("golem_assembly");
    public static final ResourceLocation GOLEM_DEPLOYMENT = TTIds.rl("golem_deployment");
    public static final ResourceLocation GOLEM_SEAL = TTIds.rl("golem_seal");
    public static final ResourceLocation FOCUS_INSCRIPTION = TTIds.rl("focus_inscription");
    public static final ResourceLocation SPELL_CAST = TTIds.rl("spell_cast");
    public static final ResourceLocation POTION_SPRAYER = TTIds.rl("potion_sprayer");
    public static final ResourceLocation CONDENSER = TTIds.rl("condenser");
    public static final ResourceLocation VIS_GENERATOR = TTIds.rl("vis_generator");
    public static final ResourceLocation FLUX_SCRUBBER = TTIds.rl("flux_scrubber");
    public static final ResourceLocation NODE_JARRING = TTIds.rl("node_jarring");
    public static final ResourceLocation PECH_TRADE = TTIds.rl("pech_trade");

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
