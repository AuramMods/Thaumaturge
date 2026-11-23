package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record DistillationRules(boolean enabled, Optional<AspectList> output) {
    private static final Codec<AspectList> DISTILLATE = Codecs.validate(AspectList.NON_EMPTY_CODEC, aspects ->
            aspects.entries().stream().mapToLong(entry -> entry.amount()).sum() <= 256
                    ? DataResult.success(aspects)
                    : DataResult.error(() -> "A distillation may provide at most 256 essentia"));
    private static final Codec<DistillationRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(DistillationRules::enabled),
                    Codecs.optionalFieldOf(DISTILLATE, "output").forGetter(DistillationRules::output))
            .apply(i, DistillationRules::new));

    public static final DataMapType<Item, DistillationRules> SMELTER = map("distillation");
    public static final DataMapType<Item, DistillationRules> ADVANCED = map("advanced_distillation");

    private static DataMapType<Item, DistillationRules> map(String path) {
        return DataMapType.builder(TTIds.rl(path), Registries.ITEM, CODEC).synced(CODEC, false).build();
    }

    public static AspectList contents(DataMapType<Item, DistillationRules> map, ItemStack input) {
        if (input.isEmpty()) return AspectList.EMPTY;
        var rule = DataMaps.get(input.getItemHolder(), map);
        if (rule == null) return AspectIndexAccess.of(input);
        return rule.enabled() ? rule.output().orElseGet(() -> AspectIndexAccess.of(input)) : AspectList.EMPTY;
    }

    public static StageTarget target(ResourceLocation family, ItemStack input, AspectList output) {
        var item = input.getItem().builtInRegistryHolder().key();
        StageTarget target = StageTarget.recipe(family, item.location()).withSubject(item);
        for (var aspect : output.entries()) {
            var key = aspect.aspect().unwrapKey();
            if (key.isPresent()) target = target.withSubject(key.get());
        }
        return target;
    }
}
