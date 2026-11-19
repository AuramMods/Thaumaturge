package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.Nullable;

/** Records the height from which a wind burst launched a player. */
@Mod.EventBusSubscriber(modid = TTIds.MODID)
public final class WindBurstLanding {
    private static final String KEY = "thaumaturge:wind_burst_landing";
    private static final int LANDING_GRACE_TICKS = 40;

    private WindBurstLanding() {}

    public static void onBlastImpulse(Entity entity, boolean wind) {
        if (entity instanceof ServerPlayer player) {
            if (wind) {
                CompoundTag launch = new CompoundTag();
                launch.putDouble("height", player.getY());
                launch.putInt("grace", LANDING_GRACE_TICKS);
                launch.putString("dimension", player.level().dimension().location().toString());
                player.getPersistentData().put(KEY, launch);
            } else {
                player.getPersistentData().remove(KEY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag launch = launch(player);
        if (launch != null) {
            float drop = (float) Math.max(0.0, launch.getDouble("height") - player.getY());
            event.setDistance(Math.min(drop, event.getDistance()));
            if (launch.getInt("grace") == 0) {
                player.getPersistentData().remove(KEY);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag launch = launch(player);
        if (launch == null) {
            return;
        }
        int grace = launch.getInt("grace");
        if (grace > 0) {
            launch.putInt("grace", grace - 1);
        } else if (player.onGround() || player.isInWater() || player.onClimbable()) {
            player.getPersistentData().remove(KEY);
        }
    }

    private static @Nullable CompoundTag launch(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag launch = data.getCompound(KEY);
        if (!player.level().dimension().location().toString().equals(launch.getString("dimension"))) {
            data.remove(KEY);
            return null;
        }
        return launch;
    }
}
