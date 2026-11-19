package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArmorDyeCraftingRuntimeTest {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("port_armor_dye", "robe");
    private static final int RED = 0xB02E26;
    private static final int RED_AND_BLUE = 0xAD5398;
    private static RobeArmorItem robe;

    @BeforeAll
    static void registerRobe() {
        var forge = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.ITEM;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            robe = new RobeArmorItem(3, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(100));
            ForgeRegistries.ITEMS.register(ID, robe);
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }

    @Test
    void nativeRecipeBlendsDyesAndPreservesTheOriginalStackAndItsData() {
        var input = new ItemStack(robe);
        input.setDamageValue(7);
        input.setHoverName(Component.literal("Field robes"));
        var recipe = new ArmorDyeRecipe(ID, CraftingBookCategory.EQUIPMENT);
        var inventory = recipeGrid(input, new ItemStack(Items.RED_DYE), new ItemStack(Items.BLUE_DYE));
        assertTrue(recipe.matches(inventory, null));

        var dyed = recipe.assemble(inventory, RegistryAccess.EMPTY);
        assertSame(robe, dyed.getItem());
        assertEquals(1, dyed.getCount());
        assertEquals(RED_AND_BLUE, robe.getColor(dyed));
        assertFalse(robe.hasCustomColor(input));
        assertEquals(7, dyed.getDamageValue());
        assertEquals("Field robes", dyed.getHoverName().getString());
        var restored = ItemStack.of(dyed.save(new CompoundTag()));
        assertEquals(RED_AND_BLUE, robe.getColor(restored));
        assertEquals(3, robe.getVisDiscount(restored));
    }

    @Test
    void nativeRecipeIncludesExistingDyeAndRejectsArmorWithoutDyeSupport() {
        var input = new ItemStack(robe);
        robe.setColor(input, RED);
        var recipe = new ArmorDyeRecipe(ID, CraftingBookCategory.EQUIPMENT);
        var inventory = recipeGrid(input, new ItemStack(Items.BLUE_DYE));
        assertEquals(RED_AND_BLUE, robe.getColor(recipe.assemble(inventory, RegistryAccess.EMPTY)));
        assertEquals(RED, robe.getColor(input));
        assertFalse(recipe.matches(recipeGrid(new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.RED_DYE)), null));
        assertEquals(RED, input.getTagElement(DyeableLeatherItem.TAG_DISPLAY).getInt(DyeableLeatherItem.TAG_COLOR));
    }

    private static CraftingContainer recipeGrid(ItemStack... ingredients) {
        var grid = new TransientCraftingContainer(mock(AbstractContainerMenu.class), 2, 2);
        for (int i = 0; i < ingredients.length; i++) {
            grid.setItem(i, ingredients[i]);
        }
        return grid;
    }
}
