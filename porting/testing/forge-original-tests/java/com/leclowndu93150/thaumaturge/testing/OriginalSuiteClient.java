package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class OriginalSuiteClient {
    private static boolean executed;

    private OriginalSuiteClient() {}

    @SubscribeEvent
    public static void test(TickEvent.ClientTickEvent event) throws Exception {
        Minecraft client = Minecraft.getInstance();
        if (executed || event.phase != TickEvent.Phase.END || client.getOverlay() != null
                || !(client.screen instanceof TitleScreen)) {
            return;
        }
        executed = true;
        long window = client.getWindow().getWindow();
        System.out.printf("Forge original tests window attributes: visible=%d focused=%d%n",
                GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_VISIBLE),
                GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED));
        try {
            runTests(Path.of(System.getProperty("thaumaturge.originalTestResults")));
        } finally {
            client.stop();
        }
    }

    private static void runTests(Path results) throws Exception {
        var classes = Path.of(System.getProperty("thaumaturge.originalTestClasses"));
        var request = LauncherDiscoveryRequestBuilder.request();
        for (String line : Files.readAllLines(classes)) {
            if (!line.isBlank()) {
                request.selectors(DiscoverySelectors.selectClass(line));
            }
        }
        var listener = new SummaryGeneratingListener();
        var console = new PrintWriter(System.out, true);
        var config = LauncherConfig.builder()
                .enableTestEngineAutoRegistration(false).addTestEngines(new JupiterTestEngine()).build();
        LauncherFactory.create(config)
                .execute(request.build(), listener, new LegacyXmlReportGeneratingListener(results, console));
        var summary = listener.getSummary();
        try (var output = new PrintWriter(Files.newBufferedWriter(results.resolve("summary.txt")))) {
            summary.printTo(output);
            summary.printFailuresTo(output);
        }
        summary.printTo(console);
        summary.printFailuresTo(console);
        if (summary.getTestsStartedCount() > 0 && summary.getTestsFoundCount() == summary.getTestsSucceededCount()
                && summary.getContainersFailedCount() == 0 && summary.getContainersAbortedCount() == 0) {
            Files.writeString(results.resolve("passed"), Long.toString(summary.getTestsSucceededCount()));
        }
    }
}
