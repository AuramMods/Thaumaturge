package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.screen.casters.FocusPouchScreen;
import com.leclowndu93150.thaumaturge.client.screen.casters.focal.FocalManipulatorScreen;
import com.leclowndu93150.thaumaturge.client.screen.construct.ArcaneBoreScreen;
import com.leclowndu93150.thaumaturge.client.screen.construct.TurretAdvancedScreen;
import com.leclowndu93150.thaumaturge.client.screen.construct.TurretBasicScreen;
import com.leclowndu93150.thaumaturge.client.screen.golem.GolemBuilderScreen;
import com.leclowndu93150.thaumaturge.client.screen.golem.GolemLogisticsScreen;
import com.leclowndu93150.thaumaturge.client.screen.golem.SealScreen;
import com.leclowndu93150.thaumaturge.client.screen.research.DeconstructionTableScreen;
import com.leclowndu93150.thaumaturge.client.screen.research.ResearchTableScreen;
import com.leclowndu93150.thaumaturge.client.screen.workbench.ArcaneWorkbenchScreen;
import com.leclowndu93150.thaumaturge.content.entity.construct.MenuTurretBasic;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTMenuScreens {
    private TTMenuScreens() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(TTMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
            MenuScreens.register(TTMenus.ARCANE_GRINDSTONE.get(), ArcaneGrindstoneScreen::new);
            MenuScreens.register(TTMenus.DECONSTRUCTION_TABLE.get(), DeconstructionTableScreen::new);
            MenuScreens.register(TTMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
            MenuScreens.register(TTMenus.SMELTER.get(), SmelterScreen::new);
            MenuScreens.register(TTMenus.SPA.get(), SpaScreen::new);
            MenuScreens.register(TTMenus.THAUMOSTATIC_HARNESS.get(), ThaumostaticHarnessScreen::new);
            MenuScreens.register(TTMenus.POTION_SPRAYER.get(), PotionSprayerScreen::new);
            MenuScreens.register(TTMenus.FOCAL_MANIPULATOR.get(), FocalManipulatorScreen::new);
            MenuScreens.register(TTMenus.GOLEM_BUILDER.get(), GolemBuilderScreen::new);
            MenuScreens.register(TTMenus.GOLEM_LOGISTICS.get(), GolemLogisticsScreen::new);
            MenuScreens.register(TTMenus.SEAL.get(), SealScreen::new);
            MenuScreens.register(TTMenus.VOID_SIPHON.get(), VoidSiphonScreen::new);
            MenuScreens.register(TTMenus.THAUMATORIUM.get(), ThaumatoriumScreen::new);
            MenuScreens.register(TTMenus.PECH.get(), PechScreen::new);
            MenuScreens.register(TTMenus.FOCUS_POUCH.get(), FocusPouchScreen::new);
            MenuScreens.register(TTMenus.TURRET_BASIC.get(), TurretBasicScreen<MenuTurretBasic>::new);
            MenuScreens.register(TTMenus.TURRET_ADVANCED.get(), TurretAdvancedScreen::new);
            MenuScreens.register(TTMenus.ARCANE_BORE.get(), ArcaneBoreScreen::new);
            MenuScreens.register(TTMenus.HAND_MIRROR.get(), HandMirrorScreen::new);
        });
    }
}
