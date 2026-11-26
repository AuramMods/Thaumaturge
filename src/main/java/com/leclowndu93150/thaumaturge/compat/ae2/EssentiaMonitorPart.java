package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.storage.StorageHelper;
import appeng.me.helpers.PlayerSource;
import appeng.parts.reporting.AbstractMonitorPart;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.util.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** AE2 storage watcher with configuration and vessel transfers restricted to essentia. */
final class EssentiaMonitorPart extends AbstractMonitorPart {
    private final boolean conversion;

    EssentiaMonitorPart(IPartItem<?> partItem, boolean conversion) {
        super(partItem, true);
        this.conversion = conversion;
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 hit) {
        if (isClientSide()) return true;
        if (!canUse(player)) return false;

        var held = player.getItemInHand(hand);
        var content = EssentiaVessels.INSTANCE.getContainedStack(held);
        if (conversion && getDisplayed() instanceof EssentiaKey key
                && content != null && key.equals(content.what())) {
            deposit(player, hand, key, content.amount());
        } else if (!isLocked() && (content != null || held.isEmpty())) {
            setConfiguredItem(content == null ? null : content.what());
            configureWatchers();
            getHost().markForSave();
        } else if (isLocked()) {
            return super.onPartActivate(player, hand, hit);
        }
        return true;
    }

    @Override
    public boolean onClicked(Player player, Vec3 hit) {
        if (!conversion) return super.onClicked(player, hit);
        return withdraw(player);
    }

    @Override
    public boolean onShiftClicked(Player player, Vec3 hit) {
        if (!conversion) return super.onShiftClicked(player, hit);
        return withdraw(player);
    }

    private boolean canUse(Player player) {
        return getMainNode().isActive() && Platform.hasPermissions(getHost().getLocation(), player);
    }

    private boolean withdraw(Player player) {
        if (isClientSide()) return true;
        if (!canUse(player)) return false;
        if (getDisplayed() instanceof EssentiaKey key) {
            getMainNode().ifPresent(grid -> {
                var inventory = grid.getStorageService().getInventory();
                var power = grid.getEnergyService();
                var source = new PlayerSource(player, this);
                var vessel = handSlot(player, InteractionHand.MAIN_HAND);
                long available = StorageHelper.poweredExtraction(power, inventory, key,
                        Integer.MAX_VALUE, source, Actionable.SIMULATE);
                long capacity = EssentiaVessels.INSTANCE.insert(vessel, key, available, Actionable.SIMULATE);
                if (capacity <= 0) return;
                long extracted = StorageHelper.poweredExtraction(power, inventory, key, capacity, source);
                long inserted = EssentiaVessels.INSTANCE.insert(vessel, key, extracted, Actionable.MODULATE);
                if (extracted > inserted) {
                    long returned = inventory.insert(key, extracted - inserted, Actionable.MODULATE, source);
                    if (getLevel() instanceof ServerLevel level) {
                        key.dropAt(level, getHost().getBlockEntity().getBlockPos(), extracted - inserted - returned);
                    }
                }
            });
        }
        return true;
    }

    private void deposit(Player player, InteractionHand hand, EssentiaKey key, long amount) {
        getMainNode().ifPresent(grid -> {
            var inventory = grid.getStorageService().getInventory();
            var power = grid.getEnergyService();
            var source = new PlayerSource(player, this);
            var vessel = handSlot(player, hand);
            long capacity = StorageHelper.poweredInsert(power, inventory, key, amount, source, Actionable.SIMULATE);
            long extracted = EssentiaVessels.INSTANCE.extract(vessel, key, capacity, Actionable.MODULATE);
            if (extracted <= 0) return;
            long inserted = StorageHelper.poweredInsert(power, inventory, key, extracted, source);
            if (extracted > inserted) {
                long returned = EssentiaVessels.INSTANCE.insert(vessel, key, extracted - inserted, Actionable.MODULATE);
                if (getLevel() instanceof ServerLevel level) {
                    key.dropAt(level, getHost().getBlockEntity().getBlockPos(), extracted - inserted - returned);
                }
            }
        });
    }

    private static EssentiaVessels.Slot handSlot(Player player, InteractionHand hand) {
        return new EssentiaVessels.Slot(
                player, () -> player.getItemInHand(hand), stack -> player.setItemInHand(hand, stack));
    }

    @Override
    public IPartModel getStaticModels() {
        return conversion
                ? selectModel(ConversionMonitorPart.MODELS_OFF, ConversionMonitorPart.MODELS_ON,
                        ConversionMonitorPart.MODELS_HAS_CHANNEL, ConversionMonitorPart.MODELS_LOCKED_OFF,
                        ConversionMonitorPart.MODELS_LOCKED_ON, ConversionMonitorPart.MODELS_LOCKED_HAS_CHANNEL)
                : selectModel(StorageMonitorPart.MODELS_OFF, StorageMonitorPart.MODELS_ON,
                        StorageMonitorPart.MODELS_HAS_CHANNEL, StorageMonitorPart.MODELS_LOCKED_OFF,
                        StorageMonitorPart.MODELS_LOCKED_ON, StorageMonitorPart.MODELS_LOCKED_HAS_CHANNEL);
    }
}
