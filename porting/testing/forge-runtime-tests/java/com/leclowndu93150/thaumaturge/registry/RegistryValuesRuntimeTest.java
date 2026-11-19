package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.decor.BlockAmber;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.Test;

public class RegistryValuesRuntimeTest {
    private RegisterEvent event(ResourceKey<? extends Registry<?>> key, ForgeRegistry<?> forge, Registry<?> vanilla) throws Exception {
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        return ctor.newInstance(key, forge, vanilla);
    }

    @Test
    void actualDeferredRegistrationResolvesNativeValuesForPredicatesBuildersAndCreativeOutput() throws Exception {
        var blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, "port_value_fixture");
        var items = DeferredRegister.create(ForgeRegistries.ITEMS, "port_value_fixture");
        var block = ForgeRegistration.registerBlock(
                blocks, "block", BlockAmber::new, BlockBehaviour.Properties.of().noOcclusion());
        var blockItem = ForgeRegistration.registerSimpleBlockItem(items, block);
        var item = ForgeRegistration.registerSimpleItem(items, "item");
        var bus = BusBuilder.builder().build();
        blocks.register(bus);
        items.register(bus);
        assertFalse(block.isPresent());
        assertFalse(item.isPresent());
        // Match GameData's native event phase: unfreeze, post, freeze.
        var blockRegistry = (ForgeRegistry<?>) ForgeRegistries.BLOCKS;
        var blockView = (net.minecraft.core.MappedRegistry<?>) BuiltInRegistries.BLOCK;
        blockView.unfreeze();
        blockRegistry.unfreeze();
        try { bus.post(event(ForgeRegistries.Keys.BLOCKS, blockRegistry, BuiltInRegistries.BLOCK)); }
        finally { blockRegistry.freeze(); blockView.freeze(); }
        var itemRegistry = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var itemView = (net.minecraft.core.MappedRegistry<?>) BuiltInRegistries.ITEM;
        itemView.unfreeze();
        itemRegistry.unfreeze();
        try { bus.post(event(ForgeRegistries.Keys.ITEMS, itemRegistry, BuiltInRegistries.ITEM)); }
        finally { itemRegistry.freeze(); itemView.freeze(); }
        blockRegistry.bake();
        assertSame(block.get(), BuiltInRegistries.BLOCK.get(block.getId()));
        assertSame(block.get(), blockItem.get().getBlock());
        var amber = block.get().defaultBlockState();
        assertTrue(amber.is(block.get()));
        assertTrue(amber.skipRendering(amber, Direction.EAST));
        assertFalse(amber.skipRendering(Blocks.GLASS.defaultBlockState(), Direction.EAST));
        var emptyWorld = EmptyBlockGetter.INSTANCE;
        assertTrue(amber.getVisualShape(emptyWorld, BlockPos.ZERO, CollisionContext.empty()).isEmpty());
        assertTrue(amber.propagatesSkylightDown(emptyWorld, BlockPos.ZERO));
        assertEquals(1.0F, amber.getShadeBrightness(emptyWorld, BlockPos.ZERO));
        var stack = new ItemStack(blockItem.get(), 12);
        assertTrue(stack.is(blockItem.get()));
        assertTrue(Ingredient.of(blockItem.get()).test(stack));

        List<ItemStack> displayed = new ArrayList<>();
        CreativeModeTab.Output output = (value, visibility) -> displayed.add(value);
        output.accept(blockItem.get());
        assertTrue(displayed.get(0).is(blockItem.get()));
        assertEquals(1, displayed.get(0).getCount());

        List<FinishedRecipe> recipes = new ArrayList<>();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item.get())
                .define('#', blockItem.get()).pattern("##")
                .unlockedBy("has_block", InventoryChangeTrigger.TriggerInstance.hasItems(blockItem.get()))
                .save(recipes::add, new ResourceLocation("port_value_fixture", "recipe"));
        var json = new JsonObject();
        recipes.get(0).serializeRecipeData(json);
        assertEquals(blockItem.getId().toString(), json.getAsJsonObject("key").getAsJsonObject("#").get("item").getAsString());
        assertEquals(item.getId().toString(), json.getAsJsonObject("result").get("item").getAsString());
    }
}
