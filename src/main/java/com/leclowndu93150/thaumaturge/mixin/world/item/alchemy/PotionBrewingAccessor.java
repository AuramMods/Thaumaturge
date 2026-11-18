package com.leclowndu93150.thaumaturge.mixin.world.item.alchemy;

import java.util.List;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PotionBrewing.class)
public interface PotionBrewingAccessor {
    @Accessor("POTION_MIXES")
    static List<?> thaumaturge$getPotionMixes() {
        throw new AssertionError("PotionBrewing accessor mixin was not applied");
    }
}
