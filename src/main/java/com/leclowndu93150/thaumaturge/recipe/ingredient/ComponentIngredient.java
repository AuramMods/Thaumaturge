package com.leclowndu93150.thaumaturge.recipe.ingredient;

import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataKey;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataPredicate;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

/** The baseline component predicate, stored through the owned native item-data backend. */
public final class ComponentIngredient extends AbstractIngredient {
    public static final MapCodec<ComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false)
                    .fieldOf("items").forGetter(ComponentIngredient::items),
            ItemDataPredicate.CODEC.fieldOf("components").forGetter(ComponentIngredient::components),
            Codecs.optionalFieldOf(com.mojang.serialization.Codec.BOOL, "strict", false).forGetter(ComponentIngredient::isStrict))
            .apply(i, ComponentIngredient::new));
    public static final CodecIngredientSerializer<ComponentIngredient> SERIALIZER = new CodecIngredientSerializer<>(CODEC);

    private final HolderSet<Item> items;
    private final ItemDataPredicate components;
    private final boolean strict;
    private final ItemStack[] stacks;

    public ComponentIngredient(HolderSet<Item> items, ItemDataPredicate components, boolean strict) {
        this.items = items;
        this.components = components;
        this.strict = strict;
        stacks = items.stream().map(item -> ItemData.create(item, 1, components.expected())).toArray(ItemStack[]::new);
    }

    public HolderSet<Item> items() { return items; }
    public ItemDataPredicate components() { return components; }
    public boolean isStrict() { return strict; }

    @Override public boolean test(ItemStack stack) {
        if (!strict) return items.contains(stack.getItem().builtInRegistryHolder()) && components.test(stack);
        for (ItemStack expected : stacks) {
            if (stack.getItem() == expected.getItem() && ItemData.comparisonFields(stack).equals(ItemData.comparisonFields(expected))
                    && stack.areCapsCompatible(expected)) return true;
        }
        return false;
    }

    @Override public ItemStack[] getItems() { return stacks; }
    @Override public boolean isSimple() { return false; }
    @Override public boolean isEmpty() { return false; }
    @Override public IIngredientSerializer<? extends net.minecraft.world.item.crafting.Ingredient> getSerializer() { return SERIALIZER; }
    @Override public JsonElement toJson() { return SERIALIZER.toJson(this); }

    public static <T> ComponentIngredient of(boolean strict, ItemDataKey<T> key, T value, ItemLike... items) {
        return of(strict, ItemDataPredicate.builder().expect(key, value).build(), items);
    }

    public static ComponentIngredient of(boolean strict, ItemDataPredicate predicate, ItemLike... items) {
        return new ComponentIngredient(HolderSet.direct(Arrays.stream(items).map(item -> item.asItem().builtInRegistryHolder()).toList()),
                predicate, strict);
    }
}
