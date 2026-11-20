package com.leclowndu93150.thaumaturge.content.equipment.hover;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class HoverEvents {
    private static final int DISRUPTION_CHECK_TICKS = 20;
    private static final float AIRBORNE_MINING_PENALTY = 5.0F;

    private HoverEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player) || !HoverManager.isHovering(player)) {
            return;
        }
        if (!HoverManager.isWearingHoverGear(player)) {
            HoverManager.setHovering(player, false);
            return;
        }
        if (player.level().dimension() == OuterLands.DIMENSION
                && !player.isCreative()
                && player.tickCount % DISRUPTION_CHECK_TICKS == 0
                && ThaumaturgeServerConfig.LABYRINTH.disruptFlight.get()) {
            HoverManager.setHovering(player, false);
            player.sendSystemMessage(Component.translatable("message.thaumaturge.hover_disrupted")
                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && HoverManager.isHovering(player)) {
            // The respawn packet replaces the client player and its temporary flight permission.
            player.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.onGround() && HoverManager.isHovering(player)) {
            event.setNewSpeed(event.getOriginalSpeed() * AIRBORNE_MINING_PENALTY);
        }
    }
}
