package com.leclowndu93150.thaumaturge.compat.curio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.compat.curio.data.TTCurioProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

class CuriosNativeRuntimeTest {
    @Test
    void nativeCuriosAttachesAnIndependentCapabilityToEachStack() {
        var ticked = new AtomicReference<ItemStack>();
        var unequipped = new AtomicReference<List<ItemStack>>();
        CuriosApi.registerCurio(Items.CLOCK, new ICurioItem() {
            @Override
            public void curioTick(SlotContext context, ItemStack stack) {
                ticked.set(stack);
            }

            @Override
            public void onUnequip(SlotContext context, ItemStack replacement, ItemStack stack) {
                unequipped.set(List.of(replacement, stack));
            }
        });

        // The focused launcher skips mod construction. Install Curios' real listener for this case.
        Consumer<AttachCapabilitiesEvent<ItemStack>> attach = new CuriosEventHandler()::attachStackCapabilities;
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, attach);
        try {
            var stack = new ItemStack(Items.CLOCK);
            var copy = stack.copy();
            var context = new SlotContext("charm", mock(LivingEntity.class), 0, false, true);
            var provider = CuriosApi.getCurio(stack).orElseThrow(AssertionError::new);
            var copiedProvider = CuriosApi.getCurio(copy).orElseThrow(AssertionError::new);

            assertSame(stack, provider.getStack());
            assertSame(copy, copiedProvider.getStack());
            assertNotSame(provider, copiedProvider);
            copiedProvider.curioTick(context);
            assertSame(copy, ticked.get());
            provider.onUnequip(context, ItemStack.EMPTY);
            assertSame(ItemStack.EMPTY, unequipped.get().get(0));
            assertSame(stack, unequipped.get().get(1));
            assertFalse(CuriosApi.getCurio(new ItemStack(Items.COMPASS)).isPresent());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(attach);
        }
    }

    @Test
    void nativeInventoryLookupHandlesAbsentAndInvalidatedProviders() {
        var wearer = mock(LivingEntity.class);
        var inventory = mock(ICuriosItemHandler.class);
        var view = LazyOptional.of(() -> inventory);
        when(wearer.getCapability(CuriosCapability.INVENTORY)).thenReturn(view);
        var context = new SlotContext("charm", wearer, 0, false, true);
        var expected = new SlotResult(context, new ItemStack(Items.CLOCK));
        when(inventory.findFirstCurio(ArgumentMatchers.<Predicate<ItemStack>>any())).thenReturn(Optional.of(expected));

        assertSame(expected, CuriosApi.getCuriosInventory(wearer).resolve()
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.is(Items.CLOCK))).orElseThrow());
        view.invalidate();
        assertTrue(CuriosApi.getCuriosInventory(wearer).resolve().isEmpty());
        assertTrue(CuriosApi.getCuriosInventory(null).resolve().isEmpty());
    }

    @Test
    void existingProviderGeneratesThePlayerSlotsInTheForgeCuriosLayout(@TempDir Path output) throws Exception {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var provider = new TTCurioProvider(
                new PackOutput(output), mock(ExistingFileHelper.class), CompletableFuture.completedFuture(registries));
        provider.run(CachedOutput.NO_CACHE).join();
        Path players = output.resolve("data/thaumaturge/curios/entities/players.json");
        try (var reader = Files.newBufferedReader(players)) {
            var json = JsonParser.parseReader(reader).getAsJsonObject();
            var slots = new HashSet<String>();
            json.getAsJsonArray("slots").forEach(slot -> slots.add(slot.getAsString()));
            assertEquals(Set.of("head", "necklace", "ring", "belt", "charm"), slots);
            assertEquals("minecraft:player", json.getAsJsonArray("entities").get(0).getAsString());
        }
    }
}
