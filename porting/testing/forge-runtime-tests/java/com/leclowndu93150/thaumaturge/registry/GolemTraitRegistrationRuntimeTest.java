package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.junit.jupiter.api.Test;

class GolemTraitRegistrationRuntimeTest {
    @Test
    void golemMaterialGetsNativeForwardHoldersAndSealUsesTheSameBoundTraits() throws Exception {
        var materials = DeferredRegister.create(GolemMaterial.REGISTRY_KEY, "port_golem_fixture");
        materials.makeRegistry(() -> new RegistryBuilder<GolemMaterial>().hasTags().disableSync());
        var material = materials.register("fixture", () -> new GolemMaterial(List.of(),
                ResourceLocation.withDefaultNamespace("textures/block/iron_block.png"), 0xffffff, 10, 2, 1,
                () -> new ItemStack(Items.IRON_INGOT), () -> new ItemStack(Items.CLOCK),
                List.of(TTGolemTraits.traitHolder(TTGolemTraits.SMART), TTGolemTraits.traitHolder(TTGolemTraits.DEFT))));

        var seals = DeferredRegister.create(SealType.REGISTRY_KEY, "port_golem_fixture");
        seals.makeRegistry(() -> new RegistryBuilder<SealType>().hasTags().disableSync());
        var seal = seals.register("fixture", () -> SealType.builder(() -> mock(ISealBehavior.class))
                .requires(TTGolemTraits.traitHolder(TTGolemTraits.SMART), TTGolemTraits.traitHolder(TTGolemTraits.DEFT))
                .forbids(TTGolemTraits.traitHolder(TTGolemTraits.CLUMSY))
                .placer(() -> Items.PAPER)
                .build());

        var bus = BusBuilder.builder().build();
        TTGolemTraits.register(bus);
        materials.register(bus);
        seals.register(bus);
        var create = new NewRegistryEvent();
        bus.post(create);
        var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        fill.invoke(create);

        // Forge sorts the custom registries by ID, which puts materials before traits and seals after them.
        postRegister(bus, GolemMaterial.REGISTRY_KEY);
        var smart = material.get().traits().get(0);
        var deft = material.get().traits().get(1);
        assertFalse(TTGolemTraits.SMART.isPresent());
        assertFalse(smart.isBound());
        assertFalse(deft.isBound());
        assertEquals(TTGolemTraits.SMART.getId(), smart.unwrapKey().orElseThrow().location());
        assertSame(smart, TTGolemTraits.traitHolder(TTGolemTraits.SMART));

        postRegister(bus, GolemTrait.REGISTRY_KEY);
        assertTrue(smart.isBound());
        assertTrue(deft.isBound());
        assertSame(TTGolemTraits.SMART.get(), smart.value());
        assertSame(TTGolemTraits.DEFT.get(), deft.value());
        assertSame(smart, TTGolemTraits.SMART.getHolder().orElseThrow());
        var clumsy = TTGolemTraits.registry().getHolderOrThrow(deft.value().opposite());
        assertSame(TTGolemTraits.CLUMSY.get(), clumsy.value());
        assertSame(deft, TTGolemTraits.registry().getHolderOrThrow(clumsy.value().opposite()));

        postRegister(bus, SealType.REGISTRY_KEY);
        assertEquals(List.of(smart, deft), seal.get().requiredTraits());
        assertEquals(List.of(clumsy), seal.get().forbiddenTraits());
        assertSame(Items.PAPER, seal.get().placer());
        assertSame(smart.value(), seal.get().requiredTraits().stream().map(Holder::value).findFirst().orElseThrow());
    }

    private static void postRegister(IEventBus bus, ResourceKey<? extends Registry<?>> registryKey) throws Exception {
        var forge = RegistryManager.ACTIVE.getRegistry(registryKey.location());
        var vanilla = BuiltInRegistries.REGISTRY.get(registryKey.location());
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        forge.unfreeze();
        try {
            bus.post(ctor.newInstance(registryKey, forge, vanilla));
        } finally {
            forge.freeze();
        }
    }
}
