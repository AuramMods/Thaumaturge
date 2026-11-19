package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.MiningSpeedProvider;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintToolEvents {
    private static final float TAINT_TOOL_SPEED_MULTIPLIER = 4.0F;

    private TaintToolEvents() {}

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getState().is(TTBlockTags.TAINT_BLOCKS)) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        if (!held.is(TTItemTags.TAINT_TOOLS)) {
            return;
        }
        Float best = bestRuleSpeed(held);
        if (best == null) {
            return;
        }
        float toolSpeed = best + (float) player.getAttributeValue(TTAttributes.MINING_EFFICIENCY.get());
        event.setNewSpeed(event.getNewSpeed() * toolSpeed * TAINT_TOOL_SPEED_MULTIPLIER);
    }

    private static @Nullable Float bestRuleSpeed(ItemStack stack) {
        var item = stack.getItem();
        if (item instanceof MiningSpeedProvider tool) {
            return tool.bestMiningSpeed(stack);
        }
        if (item instanceof DiggerItem digger) {
            return Math.max(1.0F, digger.getTier().getSpeed());
        }
        if (item instanceof SwordItem || item instanceof ShearsItem || item instanceof TridentItem) {
            return stack.getDestroySpeed(Blocks.COBWEB.defaultBlockState());
        }
        return null;
    }
}
