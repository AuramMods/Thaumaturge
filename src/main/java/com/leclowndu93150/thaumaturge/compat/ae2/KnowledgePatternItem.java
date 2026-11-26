package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.ProcessingPatternItem;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** A processing pattern that only an Arcane Assembler can accept. */
final class KnowledgePatternItem extends ProcessingPatternItem {
    KnowledgePatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable AEProcessingPattern decode(@Nullable AEItemKey what, Level level) {
        if (what == null || !what.hasTag() || !what.getTag().contains(ThaumicPatterns.KNOWLEDGE)) return null;
        try {
            return new AEProcessingPattern(what) {
                @Override
                public boolean supportsPushInputsToExternalInventory() {
                    return false;
                }
            };
        } catch (IllegalArgumentException | NullPointerException invalid) {
            return null;
        }
    }
}
