package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataContext;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Test;

class JarUpgradeTest {
    @Test
    void arcaneUpgradePreservesContentsLabelAndNameWithoutChangingInput() {
        var key = TTAspects.AER;
        var aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, key, mock(IAspect.class));
        var registries = new RegistryAccess.ImmutableRegistryAccess(List.of(aspects)).freeze();
        var contents = new EssentiaList(AspectList.EMPTY.add(aspects.getHolderOrThrow(key), 48));
        var contentsKey = TTDataComponents.ESSENTIA_CONTENTS.get();
        var filterKey = TTDataComponents.ASPECT_FILTER.get();
        ItemDataContext.withRegistries(registries, () -> {
            ItemStack jar = new ItemStack(TTItems.JAR_NORMAL.get(), 3);
            ItemData.set(jar, contentsKey, contents);
            ItemData.set(jar, filterKey, key);
            ItemData.set(jar, VanillaItemData.CUSTOM_NAME, Component.literal("Air supply"));
            IArcaneCraftingInput input = mock(IArcaneCraftingInput.class);
            when(input.size()).thenReturn(2);
            when(input.getItem(0)).thenReturn(ItemStack.EMPTY);
            when(input.getItem(1)).thenReturn(jar);
            ItemStack template = new ItemStack(TTItems.JAR_VOID.get());
            ArcaneShapelessCraftingRecipe recipe = new ArcaneShapelessCraftingRecipe(
                    "", 50, Optional.empty(), AspectList.EMPTY, template, List.of(Ingredient.of(TTItems.JAR_NORMAL.get())));

            ItemStack result = recipe.assemble(input, registries);

            assertTrue(result.is(TTItems.JAR_VOID.get()));
            assertEquals(1, result.getCount());
            assertEquals(contents, ItemData.get(result, contentsKey));
            assertEquals(key, ItemData.get(result, filterKey));
            assertEquals(ItemData.get(jar, VanillaItemData.CUSTOM_NAME), ItemData.get(result, VanillaItemData.CUSTOM_NAME));
            assertEquals(3, jar.getCount());
            assertNull(ItemData.get(template, contentsKey));
            return null;
        });
    }
}
