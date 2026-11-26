package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.util.Platform;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

final class WirelessBackpackItem extends Item {
    WirelessBackpackItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack held, UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof IWirelessAccessPoint access)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer player) || !Platform.hasPermissions(access.getLocation(), player)) {
            return InteractionResult.FAIL;
        }
        var tag = held.getOrCreateTag();
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, GlobalPos.of(level.dimension(), pos)).result().ifPresent(encoded ->
                tag.put(GolemWirelessBackpack.ACCESS_POINT, encoded));
        tag.putUUID(GolemWirelessBackpack.OWNER, player.getUUID());
        player.getInventory().setChanged();
        player.displayClientMessage(Component.translatable("item.thaumaturge.wireless_golem_backpack.linked"), true);
        return InteractionResult.CONSUME;
    }
}
