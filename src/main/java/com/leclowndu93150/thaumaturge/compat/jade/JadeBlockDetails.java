package com.leclowndu93150.thaumaturge.compat.jade;

import com.google.gson.JsonParseException;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

record JadeBlockDetails(
        ResourceLocation option,
        List<Component> summary,
        List<Component> details,
        Optional<Component> title,
        boolean hideFluid) {
    private static final Codec<Component> COMPONENT = Codec.STRING.comapFlatMap(json -> {
        try {
            Component component = Component.Serializer.fromJson(json);
            return component == null
                    ? DataResult.error(() -> "Missing Jade tooltip component")
                    : DataResult.success(component);
        } catch (JsonParseException exception) {
            return DataResult.error(() -> "Invalid Jade tooltip component: " + exception.getMessage());
        }
    }, Component.Serializer::toJson);

    static final Codec<JadeBlockDetails> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("option").forGetter(JadeBlockDetails::option),
                    COMPONENT.listOf().fieldOf("summary").forGetter(JadeBlockDetails::summary),
                    COMPONENT.listOf().fieldOf("details").forGetter(JadeBlockDetails::details),
                    Codecs.optionalFieldOf(COMPONENT, "title").forGetter(JadeBlockDetails::title),
                    Codecs.optionalFieldOf(Codec.BOOL, "hide_fluid", false).forGetter(JadeBlockDetails::hideFluid))
            .apply(instance, JadeBlockDetails::new));
}
