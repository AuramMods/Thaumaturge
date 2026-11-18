package com.leclowndu93150.thaumaturge.datamap.builtin;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.RegisterDataMapTypesEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** Retains the source pack identities while implementing behavior in actual Forge query paths. */
@EventBusSubscriber(modid = TTIds.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class BuiltinDataMaps {
    public static final DataMapType<Item, FurnaceFuel> FURNACE_FUELS = DataMapType.builder(
                    new ResourceLocation("neoforge", "furnace_fuels"), Registries.ITEM, FurnaceFuel.CODEC)
            .defaults(BuiltinDataMaps::vanillaFuels).synced(FurnaceFuel.BURN_TIME_CODEC, false).build();
    public static final DataMapType<Block, Strippable> STRIPPABLES = DataMapType.builder(
                    new ResourceLocation("neoforge", "strippables"), Registries.BLOCK, Strippable.CODEC)
            .synced(Strippable.STRIPPED_BLOCK_CODEC, false).build();
    private BuiltinDataMaps() {}
    @SubscribeEvent public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(FURNACE_FUELS); event.register(STRIPPABLES);
    }

    private static Map<ResourceKey<Item>, FurnaceFuel> vanillaFuels(Registry<Item> registry) {
        Map<ResourceKey<Item>, FurnaceFuel> values = new LinkedHashMap<>();
        AbstractFurnaceBlockEntity.getFuel().forEach((item, time) -> values.put(registry.getResourceKey(item).orElseThrow(), new FurnaceFuel(time)));
        return values;
    }

    /** Preserve explicit native item overrides (including zero); -1 is Forge's request for defaults. */
    public static int declaredBurnTime(ItemStack stack, RecipeType<?> recipe) {
        int declared = stack.getBurnTime(recipe);
        if (declared != -1 || !DataMaps.loaded(stack.getItemHolder(), FURNACE_FUELS)) return declared;
        FurnaceFuel fuel = DataMaps.get(stack.getItemHolder(), FURNACE_FUELS);
        return fuel == null ? 0 : fuel.burnTime();
    }

    public static BlockState strippedState(BlockState state) {
        Strippable stripped = DataMaps.get(state.getBlock().builtInRegistryHolder(), STRIPPABLES);
        return stripped == null ? null : stripped.strippedBlock().withPropertiesOf(state);
    }
}
