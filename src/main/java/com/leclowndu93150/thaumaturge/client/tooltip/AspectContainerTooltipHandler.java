package com.leclowndu93150.thaumaturge.client.tooltip;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.leclowndu93150.thaumaturge.compat.Java17;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class AspectContainerTooltipHandler {
    private AspectContainerTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Minecraft minecraft = Minecraft.getInstance();
        if (stack.isEmpty() || minecraft == null || minecraft.screen == null) {
            return;
        }
        IItemEssentia essentia = stack.getCapability(EssentiaCapabilities.CONTAINER).orElse(null);
        if (essentia != null) {
            renderAspectList(essentia.getAspects(), event.getToolTip());
        }

        if (stack.getItem() instanceof ILabel label) {
            ResourceKey<IAspect> filter = label.getFilteredAspect(stack);
            if (filter == null) return;
            Player player = event.getEntity();
            Level level = player == null ? minecraft.level : player.level();
            Holder<IAspect> aspect = EssentiaTransportHelper.resolve(level, filter);
            if (aspect == null) return;
            renderAspectList(AspectList.of(new AspectInstance(aspect, 1)), event.getToolTip());
        }
    }

    private static void renderAspectList(AspectList aspects, List<Component> tooltip) {
        if (aspects.isEmpty()) {
            return;
        }
        tooltip.add(
                1,
                Component.translatable("tooltip.thaumaturge.aspects.header")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xB59ED9))));
        for (AspectInstance entry : Java17.reversed(aspects.sortedByAmount())) {
            Holder<IAspect> aspect = entry.aspect();
            Component named = AspectKnowledgeAccess.isKnown(aspect)
                    ? Component.translatable(
                            "tooltip.thaumaturge.amount", AspectComponents.name(aspect), entry.amount())
                    : AspectComponents.name(aspect);
            MutableComponent line = Component.literal("  ").append(named);

            line.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(aspect.value().color())));
            tooltip.add(2, line);
        }
    }
}
