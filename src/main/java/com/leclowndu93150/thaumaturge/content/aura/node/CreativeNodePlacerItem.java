package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CreativeNodePlacerItem extends Item {
    public CreativeNodePlacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(pos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        CompoundTag nodeData = BlockItem.getBlockEntityData(context.getItemInHand());
        if (nodeData != null) {
            if (!holdsNode(nodeData)
                    || !level.setBlock(pos, TTBlocks.NODE.get().defaultBlockState(), 3)
                    || !(level.getBlockEntity(pos) instanceof BlockEntityNode node)) {
                return InteractionResult.FAIL;
            }
            node.load(nodeData.copy());
            node.setNodeType(node.getNodeType());
            node.setChanged();
            level.sendBlockUpdated(pos, node.getBlockState(), node.getBlockState(), 3);
            return InteractionResult.CONSUME;
        }
        boolean placed = NodeGenerator.createRandomNodeAt(
                level,
                pos,
                level.getRandom(),
                false,
                false,
                false,
                NodeGenerator.DEFAULT_SPECIAL_RARITY,
                NodeGenerator.DEFAULT_BASE_AURA);
        return placed ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    private static boolean holdsNode(CompoundTag data) {
        CompoundTag nodeType = new CompoundTag();
        BlockEntity.addEntityType(nodeType, TTBlockEntities.NODE.get());
        return nodeType.getString("id").equals(data.getString("id"));
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumaturge.creative_only").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
