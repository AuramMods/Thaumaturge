package com.leclowndu93150.thaumaturge.mixin.world.level.block;

import com.leclowndu93150.thaumaturge.event.block.BlockDropsEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
abstract class BlockDropsMixin {
    private static final String SIMPLE_DROP = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;"
            + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V";
    private static final String BLOCK_ENTITY_DROP = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;"
            + "Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;"
            + "Lnet/minecraft/world/level/block/entity/BlockEntity;)V";
    private static final String HARVEST = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;"
            + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;"
            + "Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;"
            + "Lnet/minecraft/world/item/ItemStack;Z)V";
    private static final String AFTER_BREAK = "Lnet/minecraft/world/level/block/state/BlockState;spawnAfterBreak("
            + "Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V";
    private static final String FOR_EACH = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V";

    @Unique private static final ThreadLocal<List<ItemEntity>> thaumaturge$dropCapture = new ThreadLocal<>();

    @WrapOperation(method = {SIMPLE_DROP, BLOCK_ENTITY_DROP}, at = @At(value = "INVOKE", target = FOR_EACH, remap = false))
    private static void thaumaturge$collectVanillaDrops(List<ItemStack> loot, Consumer<ItemStack> spawn, Operation<Void> original,
            @Share("drops") LocalRef<List<ItemEntity>> captured) {
        captured.set(thaumaturge$collectDrops(loot, spawn, original));
    }

    // Forge adds the overload with the experience flag, so the method name is stable in both namespaces.
    @WrapOperation(method = HARVEST, remap = false, at = @At(value = "INVOKE", target = FOR_EACH, remap = false))
    private static void thaumaturge$collectHarvestDrops(List<ItemStack> loot, Consumer<ItemStack> spawn, Operation<Void> original,
            @Share("drops") LocalRef<List<ItemEntity>> captured) {
        captured.set(thaumaturge$collectDrops(loot, spawn, original));
    }

    @Unique
    private static List<ItemEntity> thaumaturge$collectDrops(List<ItemStack> loot, Consumer<ItemStack> spawn,
            Operation<Void> original) {
        var drops = new ArrayList<ItemEntity>();
        var previous = thaumaturge$dropCapture.get();
        thaumaturge$dropCapture.set(drops);
        try {
            original.call(loot, spawn);
        } finally {
            if (previous == null) thaumaturge$dropCapture.remove();
            else thaumaturge$dropCapture.set(previous);
        }
        return drops;
    }

    @WrapOperation(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;"
            + "Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean thaumaturge$captureDrop(Level level, Entity drop, Operation<Boolean> original) {
        var capture = thaumaturge$dropCapture.get();
        if (capture == null) {
            return original.call(level, drop);
        }
        return capture.add((ItemEntity) drop);
    }

    @WrapOperation(method = SIMPLE_DROP, at = @At(value = "INVOKE", target = AFTER_BREAK))
    private static void thaumaturge$completeSimpleDrop(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool,
            boolean dropExperience, Operation<Void> original, @Share("drops") LocalRef<List<ItemEntity>> captured) {
        thaumaturge$completeDrop(state, level, pos, tool, dropExperience, original, captured.get(), null, null);
    }

    @WrapOperation(method = BLOCK_ENTITY_DROP, at = @At(value = "INVOKE", target = AFTER_BREAK))
    private static void thaumaturge$completeBlockEntityDrop(BlockState state, ServerLevel level, BlockPos pos,
            ItemStack tool, boolean dropExperience, Operation<Void> original, @Share("drops") LocalRef<List<ItemEntity>> captured,
            @Local(argsOnly = true) @Nullable BlockEntity blockEntity) {
        thaumaturge$completeDrop(state, level, pos, tool, dropExperience, original, captured.get(), blockEntity, null);
    }

    @WrapOperation(method = HARVEST, remap = false, at = @At(value = "INVOKE", target = AFTER_BREAK, remap = true))
    private static void thaumaturge$completeHarvest(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool,
            boolean dropExperience, Operation<Void> original, @Share("drops") LocalRef<List<ItemEntity>> captured,
            @Local(argsOnly = true) @Nullable BlockEntity blockEntity, @Local(argsOnly = true) @Nullable Entity breaker) {
        thaumaturge$completeDrop(state, level, pos, tool, dropExperience, original, captured.get(), blockEntity, breaker);
    }

    @Unique
    private static void thaumaturge$completeDrop(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool,
            boolean dropExperience, Operation<Void> original, @Nullable List<ItemEntity> captured,
            @Nullable BlockEntity blockEntity, @Nullable Entity breaker) {
        var drops = captured == null ? new ArrayList<ItemEntity>() : captured;
        var event = new BlockDropsEvent(level, pos, state, blockEntity, breaker, tool, drops);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            drops.forEach(level::addFreshEntity);
            original.call(state, level, pos, tool, dropExperience);
        }
    }
}
