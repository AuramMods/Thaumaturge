package com.leclowndu93150.thaumaturge.content.golem.logistics;

import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class MenuGolemLogisticsSortTest {
    @Test
    void displayNameSortingIgnoresCapitalization() {
        ItemStack banana = named(Items.STONE, "banana");
        ItemStack apple = named(Items.DIRT, "Apple");
        ItemStack carrot = named(Items.GRAVEL, "carrot");
        List<ItemStack> items = new ArrayList<>(List.of(carrot, banana, apple));

        MenuGolemLogistics.sortByDisplayName(items);

        assertEquals(List.of(apple, banana, carrot), items);
    }

    private static ItemStack named(Item item, String name) {
        ItemStack stack = new ItemStack(item);
        ItemData.set(stack, VanillaItemData.CUSTOM_NAME, Component.literal(name));
        return stack;
    }
}
