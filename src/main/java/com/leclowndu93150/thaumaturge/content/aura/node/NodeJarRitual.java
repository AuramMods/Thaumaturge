package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerFx;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.recipe.dust.PendingSwap;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class NodeJarRitual {
    public static final ResourceLocation RESEARCH_NODE_JAR = TTIds.rl("node_jar");

    private static final int SHELL_BREAK_MARGIN_TICKS = 20;
    private static final int JAR_SETTLE_DELAY_TICKS = 15;

    private NodeJarRitual() {}

    public static boolean tryJarNode(ServerLevel level, BlockPos nodePos, Player player) {
        if (!(level.getBlockEntity(nodePos) instanceof BlockEntityNode node) || node instanceof BlockEntityJarNode) {
            return false;
        }
        if (node.isJarring()) {
            return true;
        }
        if (!PlayerResearchAccess.of(player).hasCompleted(RESEARCH_NODE_JAR)) {
            TTActionBar.sendPurple(player, "message.thaumaturge.node_jar.no_research");
            return true;
        }
        List<BlockPos> structure = structurePositions(nodePos);
        if (!fitsStructure(level, nodePos) || structure.stream().anyMatch(pos -> DustTriggerSwapQueue.isBlocked(level, pos))) {
            TTActionBar.sendPurple(player, "message.thaumaturge.node_jar.structure");
            return true;
        }
        Block source = node.getBlockState().getBlock();
        var block = source.builtInRegistryHolder().key();
        StageTarget target = StageTarget.recipe(StageTarget.NODE_JARRING, block.location())
                .withSubject(block).withSubject(TTBlocks.JAR_NODE.get().builtInRegistryHolder().key());
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            target = target.withSubject(primal);
        }
        if (!StageRules.check(level, target, player)) {
            return true;
        }
        NodeJarringRules rules = NodeJarringRules.of(source);
        if (!rules.enabled()) {
            TTActionBar.sendPurple(player, "message.thaumaturge.node_jar.disabled");
            return true;
        }
        Map<ResourceKey<IAspect>, Integer> cost = new LinkedHashMap<>();
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            cost.put(primal, rules.visPerPrimal() * WandEconomy.CENTIVIS_PER_VIS);
        }
        if (!WandVisHelper.consumeSpecificFromHotbar(player, cost, true)) {
            TTActionBar.sendPurple(player, "message.thaumaturge.node_jar.vis", rules.visPerPrimal());
            return true;
        }
        level.playSound(null, nodePos, TTSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        Vec3 hand = player instanceof ServerPlayer serverPlayer
                ? DustTriggerFx.posToHand(serverPlayer, InteractionHand.MAIN_HAND)
                : Vec3.atCenterOf(nodePos);
        if (player instanceof ServerPlayer) {
            for (BlockPos structurePos : structure) {
                DustTriggerFx.emitBlockSparkles(level, structurePos, hand);
            }
        }
        StageAuthorization authority = StageAuthorization.capture(level, target, player);
        int lastBreak = 0;
        for (BlockPos structurePos : structure) {
            double dist = Math.sqrt(LevelSpaces.distanceSqr(level, hand, Vec3.atCenterOf(structurePos)));
            int delay = (int) (dist * DustTriggerFx.SPARKLE_TICKS_PER_BLOCK) + SHELL_BREAK_MARGIN_TICKS;
            DustTriggerSwapQueue.enqueue(level,
                    PendingSwap.clearOnly(structurePos, level.getBlockState(structurePos), delay)
                            .withAuthority(authority));
            lastBreak = Math.max(lastBreak, delay);
        }
        node.beginJarring(lastBreak + JAR_SETTLE_DELAY_TICKS, rules.degradeChance(), authority);
        return true;
    }

    static boolean shellCleared(ServerLevel level, BlockPos nodePos) {
        return structurePositions(nodePos).stream().allMatch(pos -> LoadedChunks.contains(level, pos)
                && !DustTriggerSwapQueue.isBlocked(level, pos));
    }

    static void completeJar(ServerLevel level, BlockPos nodePos, BlockEntityNode node, float degradeChance) {
        RandomSource random = level.getRandom();
        NodeModifier modifier = node.getNodeModifier();
        if (random.nextFloat() < degradeChance) {
            if (modifier == null) {
                modifier = NodeModifier.PALE;
            } else if (modifier == NodeModifier.BRIGHT) {
                modifier = null;
            } else if (modifier == NodeModifier.PALE) {
                modifier = NodeModifier.FADING;
            }
        }
        NodeData data = new NodeData(
                node.getNodeType(), Optional.ofNullable(modifier), node.getAspects(), node.getAspectsBase());
        if (!level.setBlock(nodePos, TTBlocks.JAR_NODE.get().defaultBlockState(), Block.UPDATE_ALL)) {
            return;
        }
        if (level.getBlockEntity(nodePos) instanceof BlockEntityJarNode jar) {
            jar.applyNodeData(data);
            jar.setChanged();
        }
        BlockState current = level.getBlockState(nodePos);
        level.sendBlockUpdated(nodePos, current, current, Block.UPDATE_ALL);
        Effects.bamf(level, nodePos).withSound().fancy().send();
    }

    public static boolean fitsStructure(Level level, BlockPos nodePos) {
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                if (!level.getBlockState(nodePos.offset(xx, 2, zz)).is(BlockTags.WOODEN_SLABS)) {
                    return false;
                }
                for (int yy = -1; yy <= 1; yy++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    BlockState state = level.getBlockState(nodePos.offset(xx, yy, zz));
                    if (!state.is(TTBlockTags.GLASS_BLOCKS)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<BlockPos> structurePositions(BlockPos nodePos) {
        List<BlockPos> positions = new ArrayList<>();
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                positions.add(nodePos.offset(xx, 2, zz));
                for (int yy = -1; yy <= 1; yy++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    positions.add(nodePos.offset(xx, yy, zz));
                }
            }
        }
        return positions;
    }
}
