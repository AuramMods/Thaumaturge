package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TTIds.MODID, value = Dist.DEDICATED_SERVER)
public final class ForgeGameTestReport {
    private ForgeGameTestReport() {}

    @SubscribeEvent
    public static void configureReport(ServerAboutToStartEvent event) throws ParserConfigurationException {
        GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(new File(System.getProperty("thaumaturge.gameTestReport"))));
    }
}
