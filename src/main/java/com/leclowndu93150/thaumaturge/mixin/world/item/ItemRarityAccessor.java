package com.leclowndu93150.thaumaturge.mixin.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemRarityAccessor {
    @Mutable
    @Accessor("rarity")
    void thaumaturge$setRarity(Rarity rarity);
}
