package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record VoidSiphonRules(boolean enabled, int progress, int interval, float stabilityScale, int shrinkChance,
                              Optional<ItemStack> result) {
    static final VoidSiphonRules DEFAULT = new VoidSiphonRules(true, BlockEntityVoidSiphon.PROGRESS_REQUIRED, 20,
            1.0F, 33, Optional.empty());
    private static final Codec<ItemStack> RESULT = Codecs.validate(ItemStackCodecs.CODEC, stack ->
            stack.getCount() <= stack.getMaxStackSize() ? DataResult.success(stack)
                    : DataResult.error(() -> "Void Siphon result exceeds its stack capacity"));
    static final Codec<VoidSiphonRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(VoidSiphonRules::enabled),
                    Codecs.optionalFieldOf(Codec.intRange(1, 16000), "progress", DEFAULT.progress())
                            .forGetter(VoidSiphonRules::progress),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1200), "interval", DEFAULT.interval())
                            .forGetter(VoidSiphonRules::interval),
                    Codecs.optionalFieldOf(Codec.floatRange(0.0F, 10.0F), "stability_scale", DEFAULT.stabilityScale())
                            .forGetter(VoidSiphonRules::stabilityScale),
                    Codecs.optionalFieldOf(Codec.intRange(0, 10000), "shrink_chance", DEFAULT.shrinkChance())
                            .forGetter(VoidSiphonRules::shrinkChance),
                    Codecs.optionalFieldOf(RESULT, "result").forGetter(VoidSiphonRules::result))
            .apply(i, VoidSiphonRules::new));

    public static final DataMapType<Block, VoidSiphonRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("void_siphon"), Registries.BLOCK, CODEC).build();

    ItemStack output() {
        return result().map(ItemStack::copy).orElseGet(() -> new ItemStack(TTItems.VOID_SEED.get()));
    }
}
