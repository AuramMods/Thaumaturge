package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record ResearchStage(
        String textKey,
        List<ResourceLocation> recipes,
        Optional<ResearchConstruct> construct,
        List<ResearchRequirement> obtain,
        List<ResearchRequirement> craft,
        List<KnowledgeReward> knowledge,
        List<KnowledgeReward> requiredKnowledge,
        List<ResourceLocation> requiredResearch,
        int warp)
        implements IResearchStage {
    public static final Codec<ResearchStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("text").forGetter(ResearchStage::textKey),
                    Codecs.optionalFieldOf(ResourceLocation.CODEC
                            .listOf(), "recipes", List.of())
                            .forGetter(ResearchStage::recipes),
                    Codecs.optionalFieldOf(ResearchConstruct.CODEC, "construct").forGetter(ResearchStage::construct),
                    Codecs.optionalFieldOf(ResearchRequirement.CODEC
                            .listOf(), "obtain", List.of())
                            .forGetter(ResearchStage::obtain),
                    Codecs.optionalFieldOf(ResearchRequirement.CODEC
                            .listOf(), "craft", List.of())
                            .forGetter(ResearchStage::craft),
                    Codecs.optionalFieldOf(KnowledgeReward.CODEC
                            .listOf(), "knowledge", List.of())
                            .forGetter(ResearchStage::knowledge),
                    Codecs.optionalFieldOf(KnowledgeReward.CODEC
                            .listOf(), "required_knowledge", List.of())
                            .forGetter(ResearchStage::requiredKnowledge),
                    Codecs.optionalFieldOf(ResourceLocation.CODEC
                            .listOf(), "required_research", List.of())
                            .forGetter(ResearchStage::requiredResearch),
                    Codecs.optionalFieldOf(Codec.INT, "warp", 0).forGetter(ResearchStage::warp))
            .apply(instance, ResearchStage::new));
}
