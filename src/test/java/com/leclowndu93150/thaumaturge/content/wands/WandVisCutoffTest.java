package com.leclowndu93150.thaumaturge.content.wands;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.IWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.compat.Java17;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@Mod.EventBusSubscriber(modid = TTIds.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WandVisCutoffTest {
    private static final ThreadLocal<Storage> ACTIVE_STORAGE = new ThreadLocal<>();
    private static final WandCap CAP = new WandCap(0.5F, Map.of(), 1, TTIds.rl("test_vis_cutoff_cap"));
    private static final WandRod ROD = new WandRod(
            50, 1, TTIds.rl("test_vis_cutoff_rod"), null, false, false, false, null, new IWandVisStorage() {
                @Override
                public WandVis getVis(ItemStack stack) {
                    return ACTIVE_STORAGE.get().getVis(stack);
                }

                @Override
                public void setVis(ItemStack stack, WandVis charge) {
                    ACTIVE_STORAGE.get().setVis(stack, charge);
                }
            });

    @SubscribeEvent
    public static void registerWandParts(RegisterEvent event) {
        event.register(WandCap.REGISTRY_KEY, TTIds.rl("test_vis_cutoff_cap"), () -> CAP);
        event.register(WandRod.REGISTRY_KEY, TTIds.rl("test_vis_cutoff_rod"), () -> ROD);
    }

    @AfterEach
    void clearCustomStorage() {
        ACTIVE_STORAGE.remove();
    }

    private static final class Storage implements IWandVisStorage {
        WandVis charge;
        int reads;
        final List<WandVis> writes = new ArrayList<>();

        Storage(WandVis charge) {
            this.charge = charge;
        }

        @Override
        public WandVis getVis(ItemStack stack) {
            reads++;
            return charge;
        }

        @Override
        public void setVis(ItemStack stack, WandVis charge) {
            writes.add(charge);
            this.charge = charge;
        }
    }

    private static WandVis funded() {
        Map<ResourceKey<IAspect>, Integer> pools = new LinkedHashMap<>();
        TTAspects.PRIMALS.forEach(aspect -> pools.put(aspect, 100));
        return new WandVis(pools);
    }

    private static ItemStack customWand(Storage storage) {
        ACTIVE_STORAGE.set(storage);
        ItemStack stack = ItemWand.create(TTItems.WAND.get(), CAP, ROD, false);
        ItemData.set(stack, TTDataComponents.WAND_VIS.get(), WandVis.EMPTY);
        return stack;
    }

    private static boolean pay(
            ItemStack stack, Map<ResourceKey<IAspect>, Integer> costs, boolean exact, boolean simulate) {
        return exact
                ? WandVisHelper.payExact(stack, costs, simulate)
                : WandVisHelper.payCosts(stack, null, costs, !simulate, false);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void wholePaymentReadsCustomStorageOnceAndCommitsOneCompleteSnapshot(boolean exact, boolean simulate) {
        WandVis before = funded();
        Storage storage = new Storage(before);
        ItemStack stack = customWand(storage);
        var itemTag = stack.getTag().copy();
        assertTrue(pay(stack, WandVisHelper.evenSplit(120), exact, simulate));
        assertEquals(1, storage.reads);
        assertEquals(simulate ? 0 : 1, storage.writes.size());
        int after = simulate ? 100 : exact ? 80 : 90;
        TTAspects.PRIMALS.forEach(aspect -> assertEquals(after, storage.charge.amount(aspect)));
        if (simulate) assertSame(before, storage.charge);
        else assertSame(storage.charge, Java17.first(storage.writes));
        assertEquals(WandVis.EMPTY, ItemData.get(stack, TTDataComponents.WAND_VIS.get()));
        assertEquals(itemTag, stack.getTag());
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void lateUnaffordableCostNeverPublishesEarlierDeductions(boolean exact, boolean simulate) {
        ResourceKey<IAspect> last = Java17.last(TTAspects.PRIMALS);
        WandVis before = funded().with(last, 9);
        Storage storage = new Storage(before);
        ItemStack stack = customWand(storage);
        assertFalse(pay(stack, WandVisHelper.evenSplit(120), exact, simulate));
        assertEquals(1, storage.reads);
        assertTrue(storage.writes.isEmpty());
        assertSame(before, storage.charge);
    }

    @Test
    void emptyCostMapsFailWithoutReadingOrWritingStorage() {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        assertFalse(WandVisHelper.payCosts(stack, null, Map.of(), true, false));
        assertFalse(WandVisHelper.payExact(stack, Map.of(), false));
        assertEquals(0, storage.reads);
        assertTrue(storage.writes.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void zeroAndNegativeMapEntriesKeepCutoffScalingAndCreditBehavior(boolean exact) {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        Map<ResourceKey<IAspect>, Integer> costs = new LinkedHashMap<>();
        costs.put(TTAspects.AER, 0);
        costs.put(TTAspects.TERRA, -7);
        assertTrue(pay(stack, costs, exact, false));
        assertEquals(1, storage.reads);
        assertEquals(1, storage.writes.size());
        assertEquals(100, storage.charge.amount(TTAspects.AER));
        assertEquals(exact ? 107 : 103, storage.charge.amount(TTAspects.TERRA));
    }

    @Test
    void nonPositiveWandDrawsBypassCustomStorageEntirely() {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        ItemWand wand = (ItemWand) stack.getItem();
        assertTrue(wand.drawVis(stack, null, 0.0F, false, false));
        assertTrue(wand.drawVis(stack, null, -5.0F, true, true));
        assertEquals(0, storage.reads);
        assertTrue(storage.writes.isEmpty());
    }

    @Test
    void singlePoolDrainUsesTheCutoffScaledCostAndDoesNotWriteWhenUnaffordable() {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        assertFalse(WandVisHelper.drainOne(stack, null, TTAspects.AER, 202, false));
        assertTrue(storage.writes.isEmpty());
        assertEquals(100, storage.charge.amount(TTAspects.AER));
        assertTrue(WandVisHelper.drainOne(stack, null, TTAspects.AER, -7, true));
        assertEquals(103, storage.charge.amount(TTAspects.AER));
        assertEquals(1, storage.writes.size());
    }

    @Test
    void emptyAspectWeightsUseTheCanonicalEvenSplit() {
        assertEquals(WandVisHelper.evenSplit(17), WandVisHelper.primalSplit(17, AspectList.EMPTY));
    }
}
