package com.leclowndu93150.thaumaturge.api.essentia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("removal")
class EssentiaCapabilityRuntimeTest {
    private static final Holder<IAspect> ASPECT = Holder.direct(mock(IAspect.class));
    private static final String AMOUNT = "FixtureEssentia";
    private static Item vessel;
    private static Item scanOnly;

    @BeforeAll
    static void registerItems() {
        var forge = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.ITEM;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            vessel = new FixtureItem(true);
            scanOnly = new FixtureItem(false);
            ForgeRegistries.ITEMS.register(new ResourceLocation("port_essentia", "vessel"), vessel);
            ForgeRegistries.ITEMS.register(new ResourceLocation("port_essentia", "scan_only"), scanOnly);
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }

    @Test
    void eventRegistersTheApiTokensWithNativeForge() {
        TTCapabilities.onRegisterCapabilities(new RegisterCapabilitiesEvent());
        assertTrue(EssentiaCapabilities.CONDUIT.isRegistered());
        assertTrue(EssentiaCapabilities.TRANSPORT.isRegistered());
        assertTrue(EssentiaCapabilities.STORAGE.isRegistered());
        assertTrue(EssentiaCapabilities.ASPECT_QUERY.isRegistered());
        assertTrue(EssentiaCapabilities.CONTAINER.isRegistered());
        assertTrue(EssentiaCapabilities.ITEM_STORAGE.isRegistered());
        assertTrue(AspectCapabilities.CONTAINER.isRegistered());
        assertTrue(VisRelayCapabilities.SOURCE.isRegistered());
    }

    @Test
    void nativeStacksAttachViewsWithoutRecursingAndCaptureStorageAtLookup() {
        var stack = new ItemStack(vessel);
        var live = stack.getCapability(EssentiaCapabilities.CONTAINER).orElseThrow(AssertionError::new);
        live.setAspects(AspectList.EMPTY.add(ASPECT, 5));
        var first = stack.getCapability(EssentiaCapabilities.ITEM_STORAGE);
        live.setAspects(AspectList.EMPTY.add(ASPECT, 9));
        var second = stack.getCapability(EssentiaCapabilities.ITEM_STORAGE);
        assertEquals(5, first.orElseThrow(AssertionError::new).contents().totalAmount());
        assertEquals(9, second.orElseThrow(AssertionError::new).contents().totalAmount());
        assertNotSame(first.orElseThrow(AssertionError::new), second.orElseThrow(AssertionError::new));
        assertSame(live, stack.getCapability(EssentiaCapabilities.CONTAINER).orElseThrow(AssertionError::new));

        var copy = stack.copy();
        var copiedView = copy.getCapability(EssentiaCapabilities.CONTAINER).orElseThrow(AssertionError::new);
        assertNotSame(live, copiedView);
        assertEquals(9, copiedView.getAspects().totalAmount());
        copiedView.setAspects(AspectList.EMPTY.add(ASPECT, 2));
        assertEquals(9, live.getAspects().totalAmount());
        assertEquals(2, copy.getCapability(EssentiaCapabilities.ITEM_STORAGE).orElseThrow(AssertionError::new).contents().totalAmount());
    }

    @Test
    void scanOnlyItemsDoNotOfferTransfersAndItemLookupsAreUnsided() {
        var stack = new ItemStack(scanOnly);
        assertTrue(stack.getCapability(EssentiaCapabilities.CONTAINER).isPresent());
        assertFalse(stack.getCapability(EssentiaCapabilities.ITEM_STORAGE).isPresent());
        assertFalse(stack.getCapability(EssentiaCapabilities.CONTAINER, Direction.UP).isPresent());
        assertFalse(new ItemStack(vessel).getCapability(EssentiaCapabilities.ITEM_STORAGE, Direction.UP).isPresent());
    }

    private static class FixtureItem extends Item {
        private final boolean transferable;

        FixtureItem(boolean transferable) {
            super(new Properties());
            this.transferable = transferable;
        }

        @Override
        public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
            return EssentiaCapabilities.item(() -> contents(stack), transferable ? () -> {
                var snapshot = stack.copy();
                IEssentiaItemStorage storage = mock(IEssentiaItemStorage.class);
                when(storage.contents()).thenAnswer(invocation -> contents(snapshot).getAspects());
                return storage;
            } : null);
        }

        private IItemEssentia contents(ItemStack stack) {
            return new IItemEssentia() {
                @Override
                public AspectList getAspects() {
                    return AspectList.EMPTY.add(ASPECT, stack.getOrCreateTag().getInt(AMOUNT));
                }

                @Override
                public void setAspects(AspectList aspects) {
                    stack.getOrCreateTag().putInt(AMOUNT, aspects.totalAmount());
                }
            };
        }
    }
}
