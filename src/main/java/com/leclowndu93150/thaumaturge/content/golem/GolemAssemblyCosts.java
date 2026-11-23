package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class GolemAssemblyCosts {
    private static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codecs.list(ItemStackCodecs.CODEC, 0, 16), "components")
                            .forGetter(Rule::components),
                    Codecs.optionalFieldOf(Codec.intRange(-1000, 1000), "machina_delta", 0)
                            .forGetter(Rule::machinaDelta))
            .apply(i, Rule::new));

    public static final DataMapType<GolemMaterial, Rule> MATERIAL = map(GolemMaterial.REGISTRY_KEY);
    public static final DataMapType<GolemHead, Rule> HEAD = map(GolemHead.REGISTRY_KEY);
    public static final DataMapType<GolemArm, Rule> ARM = map(GolemArm.REGISTRY_KEY);
    public static final DataMapType<GolemLeg, Rule> LEG = map(GolemLeg.REGISTRY_KEY);
    public static final DataMapType<GolemAddon, Rule> ADDON = map(GolemAddon.REGISTRY_KEY);
    public static final DataMapType<GolemTrait, Rule> TRAIT = map(GolemTrait.REGISTRY_KEY);

    private GolemAssemblyCosts() {}

    private static <T> DataMapType<T, Rule> map(ResourceKey<Registry<T>> registry) {
        return DataMapType.builder(TTIds.rl("golem_assembly"), registry, CODEC).synced(CODEC, false).build();
    }

    static <T> @Nullable Rule rule(Registry<T> registry, T entry, DataMapType<T, Rule> map) {
        return DataMaps.get(registry.wrapAsHolder(entry), map);
    }

    public record Rule(Optional<List<ItemStack>> components, int machinaDelta) {}
}
