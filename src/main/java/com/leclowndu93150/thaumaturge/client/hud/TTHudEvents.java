package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.casters.RadialFocusOverlay;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, modid = TTIds.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class TTHudEvents {
    private TTHudEvents() {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // Forge draws the experience level in EXPERIENCE_BAR, followed by the item name or spectator tooltip.
        var anchor = VanillaGuiOverlay.ITEM_NAME.id();
        event.registerAbove(
                anchor, "hover_hud", GuiBlend.alphaBlendedOverlay(new HoverHudOverlay()));
        event.registerAbove(
                anchor,
                "left_hud_stack",
                GuiBlend.alphaBlendedOverlay(new LeftHudStack(
                        List.of(CasterHudOverlay.dialGauge(), new AuraHudOverlay(), new SanityHudOverlay()))));
        event.registerAbove(
                anchor, "knowledge_gain", GuiBlend.alphaBlendedOverlay(new KnowledgeGainOverlay()));
        event.registerAbove(
                anchor, "caster_hud", GuiBlend.alphaBlendedOverlay(new CasterHudOverlay()));
        event.registerAbove(
                anchor, "recharge_hud", GuiBlend.alphaBlendedOverlay(new RechargeHudOverlay()));
        event.registerAbove(
                anchor, "radial_focus", GuiBlend.alphaBlendedOverlay(new RadialFocusOverlay()));
    }
}
