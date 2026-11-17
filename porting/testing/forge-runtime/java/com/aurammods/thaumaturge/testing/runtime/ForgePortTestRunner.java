package com.aurammods.thaumaturge.testing.runtime;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
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
        // ForgeMod normally registers this during the recipe-serializer event.
        // This focused launch performs no mod construction or registry events.
        CraftingHelper.register(ResourceLocation.withDefaultNamespace("item"), VanillaIngredientSerializer.INSTANCE);
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
}
