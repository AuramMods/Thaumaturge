package com.leclowndu93150.thaumaturge.content.equipment.runic;

import com.leclowndu93150.thaumaturge.event.combat.DamagePhaseEvent;
import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class RunicShieldingEvents {
    private static final float EFFECT_VOLUME = 0.66F;

    private RunicShieldingEvents() {}

    private static final float PITCH_BASE = 1.1F;
    private static final float PITCH_SPREAD = 0.1F;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player entity = event.player;
        if (!entity.isRemoved() && !entity.isSpectator()) {
            tickShield(entity);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShieldRescan.run(player, AttachmentData.get(player, TTAttachments.RUNIC_SHIELD));
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(DamagePhaseEvent.BeforeAbsorption event) {
        if (event.getEntity() instanceof ServerPlayer player && hasActiveShield(player)) {
            playEffect(player);
        }
    }

    private static void tickShield(Player entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            RunicShielding.tick(serverPlayer);
        }
    }

    private static void playEffect(ServerPlayer player) {
        float pitch = PITCH_BASE + player.getRandom().nextFloat() * PITCH_SPREAD;
        player.level()
                .playSound(
                        null,
                        player.blockPosition(),
                        TTSounds.RUNICSHIELDEFFECT.get(),
                        SoundSource.PLAYERS,
                        EFFECT_VOLUME,
                        pitch);
    }

    private static boolean hasActiveShield(ServerPlayer player) {
        return player.getAbsorptionAmount() > 0.0F && AttachmentData.get(player, TTAttachments.RUNIC_SHIELD).maxCharge > 0;
    }
}
