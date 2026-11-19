package com.leclowndu93150.thaumaturge.client.toast;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.network.ServerboundClearResearchFlagsPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ResearchToastEvents {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static int tickCounter;

    private ResearchToastEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isRemoved() || mc.level == null || ++tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        var knowledge = PlayerResearchAccess.of(mc.player);
        List<ResourceLocation> shown = new ArrayList<>();
        for (ResourceLocation research : knowledge.knownEntries()) {
            if (!knowledge.hasFlag(research, ResearchFlag.POPUP)) {
                continue;
            }
            mc.player
                    .level().registryAccess()
                    .lookup(IResearchEntry.REGISTRY_KEY)
                    .flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)))
                    .ifPresent(holder -> mc.getToasts()
                            .addToast(new ResearchToast(
                                    research,
                                    Component.translatable("gui.thaumaturge.research.complete"),
                                    Component.translatable(holder.value().nameKey()),
                                    EntryIconRenderer.resolveIcon(holder.value(), mc.player.tickCount))));
            knowledge.removeFlag(research, ResearchFlag.POPUP);
            shown.add(research);
        }
        for (ResourceLocation research : shown) {
            PayloadNetwork.sendToServer(
                    new ServerboundClearResearchFlagsPayload(research, List.of(ResearchFlag.POPUP)));
        }
    }
}
