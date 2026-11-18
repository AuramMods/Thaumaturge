package com.aurammods.thaumaturge.testing.runtime;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.common.crafting.conditions.*;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeDeferredRegistriesSetup;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

/** Runs the actual focused test classes after real ModLauncher/Forge transformations. */
public final class ForgePortTestRunner {
    private ForgePortTestRunner() {}

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setContextClassLoader(ForgePortTestRunner.class.getClassLoader());
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Normal Forge loading starts the global game-event bus after mod construction.
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.start();
        registerForgeEntityPrerequisites();
        // Forge's registry bake normally assigns block-state wire IDs and
        // initializes the native state caches before worlds/networking start.
        ((net.minecraftforge.registries.ForgeRegistry<?>) net.minecraftforge.registries.ForgeRegistries.BLOCKS).bake();
        // ForgeMod normally registers this during the recipe-serializer event.
        // This focused launch skips mod construction and the recipe registration event.
        CraftingHelper.register(ResourceLocation.withDefaultNamespace("item"), VanillaIngredientSerializer.INSTANCE);
        CraftingHelper.register(AndCondition.Serializer.INSTANCE);
        CraftingHelper.register(FalseCondition.Serializer.INSTANCE);
        CraftingHelper.register(ItemExistsCondition.Serializer.INSTANCE);
        CraftingHelper.register(ModLoadedCondition.Serializer.INSTANCE);
        CraftingHelper.register(NotCondition.Serializer.INSTANCE);
        CraftingHelper.register(OrCondition.Serializer.INSTANCE);
        CraftingHelper.register(TrueCondition.Serializer.INSTANCE);
        CraftingHelper.register(TagEmptyCondition.Serializer.INSTANCE);
        Path results = Path.of(System.getProperty("thaumaturge.portResults"));
        Files.createDirectories(results);
        var request = LauncherDiscoveryRequestBuilder.request();
        for (String name : Files.readAllLines(Path.of(System.getProperty("thaumaturge.portTestClasses")))) {
            if (!name.isBlank()) request.selectors(DiscoverySelectors.selectClass(name));
        }
        var summary = new SummaryGeneratingListener();
        var launcher = LauncherFactory.create(LauncherConfig.builder()
                .enableTestEngineAutoRegistration(false).addTestEngines(new JupiterTestEngine()).build());
        launcher.registerTestExecutionListeners(summary, new LegacyXmlReportGeneratingListener(results, new PrintWriter(System.out, true)));
        launcher.execute(request.build());
        var result = summary.getSummary();
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(results.resolve("summary.txt")))) {
            result.printTo(out);
            result.printFailuresTo(out);
        }
        result.printTo(new PrintWriter(System.out, true));
        result.printFailuresTo(new PrintWriter(System.out, true));
        System.out.println("FORGE_PORT_TESTS executed=" + result.getTestsStartedCount()
                + " passed=" + result.getTestsSucceededCount() + " failed=" + result.getTestsFailedCount());
        if (result.getTestsStartedCount() == 0 || result.getTestsFailedCount() != 0
                || result.getTestsAbortedCount() != 0 || result.getContainersFailedCount() != 0
                || result.getContainersAbortedCount() != 0) {
            throw new AssertionError("Forge port tests failed; see " + results);
        }
    }

    private static void registerForgeEntityPrerequisites() throws Exception {
        // Native entities require Forge fluid types and default living attributes.
        var bus = BusBuilder.builder().build();
        ForgeDeferredRegistriesSetup.setup(bus);
        var vanillaFluids = ForgeMod.class.getDeclaredField("VANILLA_FLUID_TYPES");
        vanillaFluids.setAccessible(true);
        ((DeferredRegister<?>) vanillaFluids.get(null)).register(bus);
        var forgeAttributes = ForgeMod.class.getDeclaredField("ATTRIBUTES");
        forgeAttributes.setAccessible(true);
        ((DeferredRegister<?>) forgeAttributes.get(null)).register(bus);

        var created = new NewRegistryEvent();
        bus.post(created);
        var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        fill.invoke(created);

        var registry = (ForgeRegistry<?>) ForgeRegistries.FLUID_TYPES.get();
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        bus.post(ctor.newInstance(ForgeRegistries.Keys.FLUID_TYPES, registry, null));
        registry.freeze();
        var attributes = (ForgeRegistry<?>) ForgeRegistries.ATTRIBUTES;
        boolean locked = attributes.isLocked();
        attributes.unfreeze();
        try {
            bus.post(ctor.newInstance(ForgeRegistries.Keys.ATTRIBUTES, attributes, BuiltInRegistries.ATTRIBUTE));
        } finally {
            if (locked) attributes.freeze();
        }
    }
}
