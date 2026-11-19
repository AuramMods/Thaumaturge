package com.leclowndu93150.thaumaturge.api.essentia;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class EssentiaFilterFacadeTest {
    @Test
    void filterFacadeCopiesStacksAndRejectsRebinding() {
        ItemStack input = new ItemStack(TTItems.LABEL.get());
        ItemStack labelled = EssentiaAccess.withAspectFilter(input, TTAspects.ORDO);
        assertNull(EssentiaAccess.aspectFilter(input));
        assertEquals(TTAspects.ORDO, EssentiaAccess.aspectFilter(labelled));
        ItemStack cleared = EssentiaAccess.withAspectFilter(labelled, null);
        assertNull(EssentiaAccess.aspectFilter(cleared));
        assertEquals(TTAspects.ORDO, EssentiaAccess.aspectFilter(labelled));
        assertThrows(IllegalStateException.class, () -> EssentiaAccess.bind(TTDataComponents.ASPECT_FILTER));
    }
}
