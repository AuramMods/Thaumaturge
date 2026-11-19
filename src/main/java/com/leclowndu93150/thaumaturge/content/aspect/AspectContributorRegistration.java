package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.RegisterAspectContributorsEvent;
import com.leclowndu93150.thaumaturge.registry.TTAspectContributors;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class AspectContributorRegistration {
    private AspectContributorRegistration() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TTAspectContributors.register(List.of(
                    new CrucibleAspectContributor(),
                    new InfusionAspectContributor(),
                    new CraftingAspectContributor(),
                    new CookingAspectContributor(),
                    new StonecuttingAspectContributor(),
                    new SmithingAspectContributor()));
            RegisterAspectContributorsEvent contributors = new RegisterAspectContributorsEvent();
            ModLoader.get().postEvent(contributors);
            TTAspectContributors.register(contributors.contributors());
        });
    }
}
