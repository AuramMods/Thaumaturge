package com.leclowndu93150.thaumaturge.datamap.builtin;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.datamap.*;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BuiltinDataMapRuntimeTest {
    private static final AtomicInteger QUERIES = new AtomicInteger();
    private static Item customFuel;
    private static Block customBlock;
    @TempDir Path packs;

    @BeforeAll static void registerActualOverrideItemsAndBlock() {
        var itemForge = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var itemView = (net.minecraft.core.MappedRegistry<?>) BuiltInRegistries.ITEM;
        itemForge.unfreeze(); itemView.unfreeze();
        try {
            customFuel = new Item(new Item.Properties()) {
                @Override public int getBurnTime(ItemStack stack, RecipeType<?> recipe) {
                    QUERIES.incrementAndGet();
                    return stack.getTag() != null && stack.getTag().contains("burn") ? stack.getTag().getInt("burn")
                            : recipe == RecipeType.SMOKING ? 300 : recipe == RecipeType.BLASTING ? 400 : 123;
                }
            };
            ForgeRegistries.ITEMS.register(new ResourceLocation("port_builtin_maps", "custom_fuel"), customFuel);
        } finally { itemForge.freeze(); itemView.freeze(); }
        var blockForge = (ForgeRegistry<?>) ForgeRegistries.BLOCKS;
        var blockView = (net.minecraft.core.MappedRegistry<?>) BuiltInRegistries.BLOCK;
        blockForge.unfreeze(); blockView.unfreeze();
        try {
            customBlock = new Block(BlockBehaviour.Properties.of()) {
                @Override public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
                    return action == ToolActions.AXE_STRIP ? Blocks.DIAMOND_BLOCK.defaultBlockState() : null;
                }
            };
            ForgeRegistries.BLOCKS.register(new ResourceLocation("port_builtin_maps", "custom_block"), customBlock);
        } catch (RuntimeException | Error failure) {
            try { blockForge.freeze(); blockView.freeze(); } catch (RuntimeException cleanup) { failure.addSuppressed(cleanup); }
            throw failure;
        }
        blockForge.freeze(); blockView.freeze();
        blockForge.bake();
    }
    @BeforeEach void clearNativeMapFixtures() {
        DataMaps.replace(BuiltInRegistries.ITEM, Map.of()); DataMaps.replace(BuiltInRegistries.BLOCK, Map.of());
        ForgeHooks.updateBurns(); QUERIES.set(0);
    }
    @AfterEach void releaseNativeMapFixtures() {
        DataMaps.replace(BuiltInRegistries.ITEM, Map.of()); DataMaps.replace(BuiltInRegistries.BLOCK, Map.of());
    }
    private void fuel(Map<net.minecraft.resources.ResourceKey<Item>, FurnaceFuel> values) { DataMaps.replace(BuiltInRegistries.ITEM, Map.of(BuiltinDataMaps.FURNACE_FUELS, values)); }
    private void stripping(Map<net.minecraft.resources.ResourceKey<Block>, Strippable> values) { DataMaps.replace(BuiltInRegistries.BLOCK, Map.of(BuiltinDataMaps.STRIPPABLES, values)); }
    private int time(ItemStack stack, RecipeType<?> type) { return ForgeHooks.getBurnTime(stack, type); }
    private DataMapCatalog catalog() {
        var catalog = new DataMapCatalog(); var bus = BusBuilder.builder().markerType(IModBusEvent.class).build();
        bus.addListener(BuiltinDataMaps::onRegister); bus.post(new RegisterDataMapTypesEvent(catalog)); catalog.freeze(); return catalog;
    }
    private void load(String name, String fuelJson, String stripJson) throws Exception {
        var root = packs.resolve(name);
        Files.createDirectories(root.resolve("data/neoforge/data_maps/item")); Files.createDirectories(root.resolve("data/neoforge/data_maps/block"));
        Files.writeString(root.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":15,\"description\":\"Native builtin-map fixture\"}}");
        if (fuelJson != null) Files.writeString(root.resolve("data/neoforge/data_maps/item/furnace_fuels.json"), fuelJson);
        if (stripJson != null) Files.writeString(root.resolve("data/neoforge/data_maps/block/strippables.json"), stripJson);
        var owner = new RegistryAccess.ImmutableRegistryAccess(List.of(BuiltInRegistries.ITEM, BuiltInRegistries.BLOCK)).freeze();
        var loader = new DataMapLoader(owner, ICondition.IContext.EMPTY, catalog());
        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(new PathPackResources(name, root, false)))) {
            SimpleReloadInstance.of(manager, List.of(loader), Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE)).done().join(); loader.apply(owner);
        }
        assertTrue(loader.diagnostics().isEmpty(), loader.diagnostics().toString());
    }

    @Test void retainedSchemasHaveTheirActualSourceIdsAndFullAndNetworkFormats() {
        var catalog = catalog(); assertEquals(2, catalog.types().size());
        assertSame(BuiltinDataMaps.FURNACE_FUELS, catalog.get(Registries.ITEM, new ResourceLocation("neoforge", "furnace_fuels")));
        assertSame(BuiltinDataMaps.STRIPPABLES, catalog.get(Registries.BLOCK, new ResourceLocation("neoforge", "strippables")));
        assertEquals(new FurnaceFuel(4800), FurnaceFuel.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("4800")).result().orElseThrow());
        assertEquals(new FurnaceFuel(4800), FurnaceFuel.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"burn_time\":4800}")).result().orElseThrow());
        assertEquals(JsonParser.parseString("{\"burn_time\":4800}"), FurnaceFuel.CODEC.encodeStart(JsonOps.INSTANCE, new FurnaceFuel(4800)).result().orElseThrow());
        assertEquals(JsonParser.parseString("4800"), BuiltinDataMaps.FURNACE_FUELS.networkCodec().encodeStart(JsonOps.INSTANCE, new FurnaceFuel(4800)).result().orElseThrow());
        for (String invalid : List.of("0", "-1", "{\"burn_time\":0}", "{}")) assertTrue(FurnaceFuel.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(invalid)).error().isPresent());
        assertEquals(new Strippable(Blocks.STRIPPED_OAK_LOG), Strippable.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"minecraft:stripped_oak_log\"")).result().orElseThrow());
        assertEquals(new Strippable(Blocks.STRIPPED_OAK_LOG), Strippable.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"stripped_block\":\"minecraft:stripped_oak_log\"}")).result().orElseThrow());
        assertEquals(JsonParser.parseString("\"minecraft:stripped_oak_log\""), BuiltinDataMaps.STRIPPABLES.networkCodec().encodeStart(JsonOps.INSTANCE, new Strippable(Blocks.STRIPPED_OAK_LOG)).result().orElseThrow());
    }

    @Test void nativeDefaultsAndReloadedFuelOverridesHonorRemovalsAndFileReplacement() throws Exception {
        assertEquals(1600, time(new ItemStack(Items.COAL), null)); assertEquals(0, time(new ItemStack(Items.PAPER), null));
        assertFalse(DataMaps.loaded(Items.COAL.builtInRegistryHolder(), BuiltinDataMaps.FURNACE_FUELS));
        load("first", "{\"values\":{\"minecraft:paper\":4800},\"remove\":[\"minecraft:coal\"]}", null);
        assertTrue(DataMaps.loaded(Items.COAL.builtInRegistryHolder(), BuiltinDataMaps.FURNACE_FUELS));
        assertEquals(0, time(new ItemStack(Items.COAL), null)); assertEquals(4800, time(new ItemStack(Items.PAPER), null));
        assertEquals(2400, time(new ItemStack(Items.BLAZE_ROD), null));
        load("replacement", "{\"replace\":true,\"values\":{\"minecraft:paper\":7000}}", null);
        assertEquals(7000, time(new ItemStack(Items.PAPER), null)); assertEquals(0, time(new ItemStack(Items.BLAZE_ROD), null));
        load("removed-files", null, null);
        assertEquals(0, time(new ItemStack(Items.PAPER), null)); assertEquals(1600, time(new ItemStack(Items.COAL), null));
        fuel(Map.of()); assertEquals(0, time(new ItemStack(Items.COAL), null));
        DataMaps.replace(BuiltInRegistries.ITEM, Map.of()); assertEquals(1600, time(new ItemStack(Items.COAL), null));
    }

    @Test void nativeItemOverridesRemainNbtAndRecipeSensitiveAndAreInvokedOnce() {
        fuel(Map.of(customFuel.builtInRegistryHolder().key(), new FurnaceFuel(4800)));
        var stack = new ItemStack(customFuel);
        assertEquals(123, time(stack, RecipeType.SMELTING)); assertEquals(1, QUERIES.getAndSet(0));
        assertEquals(300, time(stack, RecipeType.SMOKING)); assertEquals(1, QUERIES.getAndSet(0));
        stack.getOrCreateTag().putInt("burn", 0); assertEquals(0, time(stack, null)); assertEquals(1, QUERIES.getAndSet(0));
        stack.getOrCreateTag().putInt("burn", -1); assertEquals(4800, time(stack, null)); assertEquals(1, QUERIES.getAndSet(0));
        assertEquals(0, time(ItemStack.EMPTY, null)); assertEquals(0, QUERIES.get());
    }

    @Test void actualForgeFuelEventSeesTheMappedValueAndCanStillOverrideIt() {
        fuel(Map.of(Items.PAPER.builtInRegistryHolder().key(), new FurnaceFuel(4800)));
        var calls = new AtomicInteger();
        Consumer<FurnaceFuelBurnTimeEvent> listener = event -> {
            assertEquals(4800, event.getBurnTime()); assertEquals(RecipeType.SMELTING, event.getRecipeType());
            assertEquals(Items.PAPER, event.getItemStack().getItem()); calls.incrementAndGet(); event.setBurnTime(8123);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try { assertEquals(8123, time(new ItemStack(Items.PAPER), RecipeType.SMELTING)); assertEquals(1, calls.get()); }
        finally { MinecraftForge.EVENT_BUS.unregister(listener); }
    }

    @Test void actualNativeFurnaceVariantsAndFuelPredicateUseTheMappedQuery() throws Exception {
        fuel(Map.of(Items.PAPER.builtInRegistryHolder().key(), new FurnaceFuel(4800)));
        var method = AbstractFurnaceBlockEntity.class.getDeclaredMethod("getBurnDuration", ItemStack.class); method.setAccessible(true);
        assertEquals(4800, method.invoke(new FurnaceBlockEntity(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState()), new ItemStack(Items.PAPER)));
        assertEquals(2400, method.invoke(new SmokerBlockEntity(BlockPos.ZERO, Blocks.SMOKER.defaultBlockState()), new ItemStack(Items.PAPER)));
        assertEquals(2400, method.invoke(new BlastFurnaceBlockEntity(BlockPos.ZERO, Blocks.BLAST_FURNACE.defaultBlockState()), new ItemStack(Items.PAPER)));
        assertTrue(AbstractFurnaceBlockEntity.isFuel(new ItemStack(Items.PAPER))); assertFalse(AbstractFurnaceBlockEntity.isFuel(new ItemStack(Items.COAL)));
    }

    @Test void mappedStrippingCopiesAllSharedPropertiesThroughBothActualAxePaths() throws Exception {
        stripping(Map.of(Blocks.OAK_STAIRS.builtInRegistryHolder().key(), new Strippable(Blocks.SPRUCE_STAIRS)));
        var original = Blocks.OAK_STAIRS.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .setValue(BlockStateProperties.HALF, Half.TOP).setValue(BlockStateProperties.WATERLOGGED, true);
        var expected = Blocks.SPRUCE_STAIRS.withPropertiesOf(original);
        assertEquals(expected, AxeItem.getAxeStrippingState(original));
        var method = AxeItem.class.getDeclaredMethod("getStripped", BlockState.class); method.setAccessible(true);
        assertEquals(Optional.of(expected), method.invoke(Items.DIAMOND_AXE, original));
        stripping(Map.of(Blocks.OAK_LOG.builtInRegistryHolder().key(), new Strippable(Blocks.SPRUCE_PLANKS)));
        assertEquals(Blocks.SPRUCE_PLANKS.defaultBlockState(), AxeItem.getAxeStrippingState(Blocks.OAK_LOG.defaultBlockState()));
    }

    private UseOnContext context(ItemStack tool) {
        return new UseOnContext(null, null, InteractionHand.MAIN_HAND, tool, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false));
    }
    @Test void nativeToolActionGatingBlockOverridesAndEventOverridesArePreserved() {
        stripping(Map.of(Blocks.OAK_LOG.builtInRegistryHolder().key(), new Strippable(Blocks.STRIPPED_SPRUCE_LOG),
                customBlock.builtInRegistryHolder().key(), new Strippable(Blocks.STRIPPED_OAK_LOG)));
        var log = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        var tool = context(new ItemStack(Items.DIAMOND_AXE));
        for (boolean simulate : List.of(true, false)) assertEquals(Blocks.STRIPPED_SPRUCE_LOG.withPropertiesOf(log), log.getToolModifiedState(tool, ToolActions.AXE_STRIP, simulate));
        assertNull(log.getToolModifiedState(context(new ItemStack(Items.STICK)), ToolActions.AXE_STRIP, true));
        assertEquals(Blocks.DIAMOND_BLOCK.defaultBlockState(), customBlock.defaultBlockState().getToolModifiedState(tool, ToolActions.AXE_STRIP, true));
        Consumer<BlockEvent.BlockToolModificationEvent> listener = event -> event.setFinalState(Blocks.GOLD_BLOCK.defaultBlockState());
        MinecraftForge.EVENT_BUS.addListener(listener);
        try { assertEquals(Blocks.GOLD_BLOCK.defaultBlockState(), log.getToolModifiedState(tool, ToolActions.AXE_STRIP, true)); }
        finally { MinecraftForge.EVENT_BUS.unregister(listener); }
    }

    @Test void strippingPackChangesAndRemovalRetainNativeVanillaFallback() throws Exception {
        var oak = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        load("strip-first", null, "{\"values\":{\"minecraft:oak_log\":\"minecraft:stripped_spruce_log\"}}");
        assertEquals(Blocks.STRIPPED_SPRUCE_LOG.withPropertiesOf(oak), AxeItem.getAxeStrippingState(oak));
        load("strip-empty", null, null);
        assertEquals(Blocks.STRIPPED_OAK_LOG.withPropertiesOf(oak), AxeItem.getAxeStrippingState(oak));
        assertNull(AxeItem.getAxeStrippingState(Blocks.STONE.defaultBlockState()));
    }
}
