package com.leclowndu93150.thaumaturge.itemdata;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ItemDataLoot {
    private static final DeferredRegister<LootItemFunctionType> FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, TTIds.MODID);
    public static final RegistryObject<LootItemFunctionType> COPY_ITEM_DATA =
            FUNCTIONS.register("copy_item_data", () -> new LootItemFunctionType(new CopyItemDataFunction.Serializer()));

    private ItemDataLoot() {}

    /** Encodes item data with the generator's registry lookup for a freshly created loot stack. */
    public static <T> LootItemConditionalFunction.Builder<?> setData(
            ItemLike item, ItemDataKey<T> key, T value, HolderLookup.Provider registries) {
        return ItemDataContext.withRegistries(registries, () -> {
            var stack = new ItemStack(item);
            ItemData.set(stack, key, value);
            CompoundTag tag = stack.getTag();
            return SetNbtFunction.setTag(tag == null ? new CompoundTag() : tag);
        });
    }

    public static void register(IEventBus bus) { FUNCTIONS.register(bus); }
}
