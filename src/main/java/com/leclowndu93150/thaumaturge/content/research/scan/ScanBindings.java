package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedBlock;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class ScanBindings implements ScanningManager.Bindings {
    @Override
    public @Nullable Component scanDenial(Player player, @Nullable ScanTarget scan,
                                          @Nullable ResourceKey<ScanEntry> entry, @Nullable ResourceLocation research) {
        if (!(player.level() instanceof ServerLevel level) || !StageRules.of(level).hasRules(StageTarget.SCAN)) {
            return null;
        }
        ResourceLocation id = entry == null ? research : entry.location();
        StageTarget target = id == null
                ? StageTarget.family(StageTarget.SCAN)
                : StageTarget.recipe(StageTarget.SCAN, id);
        if (entry != null) {
            target = target.withSubject(entry);
        }
        if (research != null) {
            target = target.withSubject(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research));
        }
        if (scan instanceof ScannedBlock block) {
            target = target.withSubject(level.getBlockState(block.pos()).getBlock().builtInRegistryHolder().key());
        } else if (scan != null) {
            Entity creature = scan.creature();
            if (creature != null) {
                target = target.withSubject(creature.getType().builtInRegistryHolder().key());
            } else {
                ItemStack stack = scan.carriedStack();
                if (!stack.isEmpty()) target = target.withSubject(stack.getItem().builtInRegistryHolder().key());
            }
        }
        var denial = StageRules.denial(level, target, player);
        return denial == null ? null : denial.message();
    }

    @Override
    public boolean progressResearch(Player player, ResourceLocation research) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        boolean hasEntry = serverPlayer
                .level().registryAccess()
                .lookupOrThrow(IResearchEntry.REGISTRY_KEY)
                .get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research))
                .isPresent();
        PlayerKnowledge knowledge = (PlayerKnowledge) PlayerResearchAccess.of(serverPlayer);
        if (hasEntry) {
            if (!knowledge.knows(research)) {
                if (!ResearchManager.unlock(serverPlayer, research)) {
                    return false;
                }
                ResearchManager.advanceStage(serverPlayer, research);
                return true;
            }
            return ResearchManager.advanceStage(serverPlayer, research);
        }
        if (knowledge.knows(research)) {
            return false;
        }
        if (!ResearchManager.checkStage(serverPlayer, research)) return false;
        knowledge.learn(research);
        knowledge.markComplete(research);
        knowledge.queueSync(serverPlayer);
        return true;
    }

    @Override
    public boolean addKnowledge(Player player, KnowledgeType type, ResourceLocation category, int amount) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        ResearchGrants.grantConvertedKnowledge(serverPlayer, type, amount);
        return true;
    }

    @Override
    public AspectList itemAspects(ItemStack stack) {
        return AspectIndexAccess.index().of(stack);
    }

    @Override
    public AspectList entityAspects(Entity entity) {
        return EntityAspects.of(entity);
    }

    @Override
    public boolean hidesItemForm(BlockState state) {
        return state.is(TTBlocks.NODE.get()) || state.is(TTBlocks.SILVERWOOD_NODE_LOG.get());
    }
}
