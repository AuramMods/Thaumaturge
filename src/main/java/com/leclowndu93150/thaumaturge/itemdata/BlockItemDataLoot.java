package com.leclowndu93150.thaumaturge.itemdata;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Native Forge loot function registration, separate from unrelated loot API conversions. */
public final class BlockItemDataLoot {
    private static final DeferredRegister<LootItemFunctionType> FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, "thaumaturge");
    public static final RegistryObject<LootItemFunctionType> COPY_ITEM_DATA =
            FUNCTIONS.register("copy_item_data", () -> new LootItemFunctionType(new CopyItemDataFunction.Serializer()));

    private BlockItemDataLoot() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); }
}
