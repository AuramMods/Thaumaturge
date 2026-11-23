package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerFx;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.recipe.dust.PendingSwap;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import com.leclowndu93150.thaumaturge.recipe.RecipeHolder;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID)
public final class SalisMundusItem extends Item {
    private static final int SWAP_DELAY_TICKS = 50;

    public SalisMundusItem(Item.Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void allowUseOnCraftingTable(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isCrouching() && event.getItemStack().is(TTItems.SALIS_MUNDUS.get())) {
            event.setUseBlock(Event.Result.DENY);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (!player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        player.swing(context.getHand());
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        if (DustTriggerSwapQueue.isBlocked(level, pos)) {
            return InteractionResult.PASS;
        }
        BlockState clicked = level.getBlockState(pos);
        DustTriggerInput input = new DustTriggerInput(stack, level, pos, clicked);
        Optional<RecipeHolder<DustTrigger>> match =
                RecipeAccess.of(serverLevel.getRecipeManager()).getRecipeFor(TTRecipeTypes.DUST_TRIGGER.get(), input, level);
        if (match.isEmpty()) {
            return InteractionResult.PASS;
        }
        RecipeHolder<DustTrigger> holder = match.get();
        DustTrigger trigger = holder.value();
        if (!trigger.doesPassGate(player)) {
            Thaumaturge.LOGGER.debug(
                    "Salis Mundus trigger {} blocked by research gate {}",
                    holder.id(),
                    trigger.researchGate().orElse(null));
            TTActionBar.sendPurple(player, "message.thaumaturge.salis_mundus.no_research");
            return InteractionResult.PASS;
        }
        var target = trigger.stageTarget(input);
        if (!StageRules.check(serverLevel, target, player)) {
            return InteractionResult.PASS;
        }
        ItemStack result = trigger.assemble(input, level.registryAccess());
        if (result.isEmpty()) {
            return InteractionResult.PASS;
        }
        DustTriggerPlacement placement = trigger.isMultiblock() ? trigger.findPlacement(input) : null;
        if (trigger.isMultiblock() && (placement == null
                || !trigger.tryExecute(input, player, placement, context.getClickedFace()))) {
            return InteractionResult.PASS;
        }
        ItemStack consumed = stack.copyWithCount(1);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        var authority = StageAuthorization.capture(serverLevel, target, player);
        if (!trigger.isMultiblock()) {
            PendingSwap swap = result.getItem() instanceof BlockItem blockItem
                    ? PendingSwap.placeBlock(pos, clicked, blockItem.getBlock().defaultBlockState(), SWAP_DELAY_TICKS)
                    : PendingSwap.dropItem(pos, clicked, result, SWAP_DELAY_TICKS);
            DustTriggerSwapQueue.enqueue(serverLevel, swap.withAuthority(authority));
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Vec3 hitWorld = context.getClickLocation();
            DustTriggerFx.emitUseBurst(serverLevel, serverPlayer, context.getHand(), pos);
            DustTriggerFx.emitTriggerSparkles(serverLevel, serverPlayer, pos, trigger, hitWorld, placement);
            serverPlayer.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), result.getCount());
            ResearchProgressionEvents.recordCrafted(serverPlayer, result);
            CriteriaTriggers.RECIPE_CRAFTED.trigger(serverPlayer, holder.id(), List.of(consumed));
        }
        level.playSound(
                null,
                pos,
                TTSounds.DUST.get(),
                SoundSource.PLAYERS,
                0.33F,
                1.0F + (float) level.getRandom().nextGaussian() * 0.05F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}
