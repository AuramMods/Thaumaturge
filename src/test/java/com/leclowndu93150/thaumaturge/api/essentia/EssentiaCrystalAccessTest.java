package com.leclowndu93150.thaumaturge.api.essentia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataContext;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataKey;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.serialization.Lifecycle;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EssentiaCrystalAccessTest {
    private Item crystal;
    private ItemDataKey<AspectInstance> component;
    private Holder<IAspect> aspect;
    private RegistryAccess registries;
    private Object savedItem;
    private Object savedComponent;

    @BeforeEach
    void prepareBindings() throws ReflectiveOperationException {
        savedItem = field("crystalItem").get(null);
        savedComponent = field("aspectComponent").get(null);
        field("crystalItem").set(null, null);
        field("aspectComponent").set(null, null);
        crystal = Items.AMETHYST_SHARD;
        component = TTDataComponents.CRYSTAL_ASPECT.get();
        var aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, TTAspects.AER, mock(IAspect.class));
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(aspects)).freeze();
        aspect = aspects.getHolderOrThrow(TTAspects.AER);
    }

    @AfterEach
    void restoreBindings() throws ReflectiveOperationException {
        field("crystalItem").set(null, savedItem);
        field("aspectComponent").set(null, savedComponent);
    }

    @Test
    void rejectsUseBeforeBindingAndRejectsDuplicateBinding() {
        assertThrows(IllegalStateException.class, () -> EssentiaCrystalAccess.state(ItemStack.EMPTY));
        assertThrows(IllegalStateException.class, () -> EssentiaCrystalAccess.create(aspect, 1));
        bind();
        assertThrows(IllegalStateException.class, this::bind);
    }

    @Test
    void emptyCreationDoesNotRequireBinding() {
        assertSame(ItemStack.EMPTY, EssentiaCrystalAccess.create(aspect, 0));
        assertSame(ItemStack.EMPTY, EssentiaCrystalAccess.create(aspect, -1));
    }

    @Test
    void classifiesOnlyGenuineSingleAspectCrystals() {
        bind();
        ItemDataContext.withRegistries(registries, () -> {
            ItemStack unconfigured = new ItemStack(crystal);
            assertEquals(EssentiaCrystalAccess.State.UNCONFIGURED, EssentiaCrystalAccess.state(unconfigured));
            assertNull(EssentiaCrystalAccess.aspect(unconfigured));
            ItemData.set(unconfigured, component, new AspectInstance(aspect, 2));
            assertEquals(EssentiaCrystalAccess.State.MALFORMED, EssentiaCrystalAccess.state(unconfigured));
            assertFalse(EssentiaCrystalAccess.isConfigured(unconfigured));
            ItemStack foreign = new ItemStack(Items.DIRT);
            ItemData.set(foreign, component, new AspectInstance(aspect, 1));
            assertEquals(EssentiaCrystalAccess.State.NOT_CRYSTAL, EssentiaCrystalAccess.state(foreign));
            assertNull(EssentiaCrystalAccess.aspect(foreign));
            return null;
        });
    }

    @Test
    void creationReturnsIndependentStacksWithOneAspectUnitPerCrystal() {
        bind();
        ItemDataContext.withRegistries(registries, () -> {
            ItemStack first = EssentiaCrystalAccess.create(aspect, 3);
            ItemStack second = EssentiaCrystalAccess.create(aspect, 3);
            assertEquals(3, first.getCount());
            assertEquals(new AspectInstance(aspect, 1), ItemData.get(first, component));
            assertSame(aspect, EssentiaCrystalAccess.aspect(first));
            assertEquals(EssentiaCrystalAccess.State.CONFIGURED, EssentiaCrystalAccess.state(first));
            ItemData.remove(first, component);
            first.setCount(1);
            assertEquals(3, second.getCount());
            assertTrue(EssentiaCrystalAccess.isConfigured(second));
            return null;
        });
    }

    private void bind() {
        EssentiaCrystalAccess.bind(() -> crystal, () -> component);
    }

    private static Field field(String name) throws ReflectiveOperationException {
        Field field = EssentiaCrystalAccess.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
