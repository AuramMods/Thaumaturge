package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.serialization.IngredientCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class SpaRules {
    private static final int DEFAULT_AMOUNT = 1000;
    private static final Codec<Integer> AMOUNT = Codec.intRange(1, BlockEntitySpa.TANK_CAPACITY);
    private static final Codec<Fluid> FLUID = BuiltInRegistries.FLUID.byNameCodec();
    private static final Mixing WATER_MIX = new Mixing(true, DEFAULT_AMOUNT, Optional.empty(), 1, Optional.empty());
    private static final Placement DEFAULT_PLACEMENT = new Placement(true, DEFAULT_AMOUNT, Optional.empty());

    public static final DataMapType<Fluid, Mixing> MIXING =
            DataMapType.builder(TTIds.rl("spa_mixing"), Registries.FLUID, Mixing.CODEC).synced(Mixing.CODEC, false).build();
    public static final DataMapType<Fluid, Placement> PLACEMENT =
            DataMapType.builder(TTIds.rl("spa_placement"), Registries.FLUID, Placement.CODEC).build();

    private SpaRules() {}

    static @Nullable Mixing mixing(Fluid fluid) {
        Mixing rule = DataMaps.get(fluid.builtInRegistryHolder(), MIXING);
        return rule != null ? rule : fluid == Fluids.WATER ? WATER_MIX : null;
    }

    static Placement placement(Fluid fluid) {
        Placement rule = DataMaps.get(fluid.builtInRegistryHolder(), PLACEMENT);
        return rule == null ? DEFAULT_PLACEMENT : rule;
    }

    static @Nullable BlockState source(Fluid fluid) {
        var state = fluid instanceof FlowingFluid flowing ? flowing.getSource().defaultFluidState()
                : fluid.defaultFluidState();
        BlockState block = state.createLegacyBlock();
        return block.is(Blocks.AIR) || !block.getFluidState().isSource() ? null : block;
    }

    public record Mixing(boolean enabled, int amount, Optional<Ingredient> additive, int items, Optional<Fluid> result) {
        private static final Codec<Mixing> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(Mixing::enabled),
                        Codecs.optionalFieldOf(AMOUNT, "amount", DEFAULT_AMOUNT).forGetter(Mixing::amount),
                        Codecs.optionalFieldOf(IngredientCodecs.CODEC, "additive").forGetter(Mixing::additive),
                        Codecs.optionalFieldOf(Codec.intRange(1, 64), "items", 1).forGetter(Mixing::items),
                        Codecs.optionalFieldOf(FLUID, "result").forGetter(Mixing::result))
                .apply(i, Mixing::new));

        boolean accepts(ItemStack stack) {
            return additive.map(ingredient -> ingredient.test(stack)).orElseGet(() -> stack.is(TTItems.BATH_SALTS.get()));
        }
    }

    public record Placement(boolean enabled, int amount, Optional<Fluid> result) {
        private static final Codec<Placement> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(Placement::enabled),
                        Codecs.optionalFieldOf(AMOUNT, "amount", DEFAULT_AMOUNT).forGetter(Placement::amount),
                        Codecs.optionalFieldOf(FLUID, "result").forGetter(Placement::result))
                .apply(i, Placement::new));
    }
}
