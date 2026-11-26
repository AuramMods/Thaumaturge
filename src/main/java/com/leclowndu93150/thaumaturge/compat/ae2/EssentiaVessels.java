package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaTransferFeedback;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaItemStorage;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

final class EssentiaVessels implements ContainerItemStrategy<EssentiaKey, EssentiaVessels.Slot> {
    static final EssentiaVessels INSTANCE = new EssentiaVessels();

    private EssentiaVessels() {}

    record Slot(Player player, Supplier<ItemStack> get, Consumer<ItemStack> set) {}

    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        var storage = storage(stack);
        if (storage == null) {
            var aspect = EssentiaCrystalAccess.aspect(stack);
            var key = aspect == null ? null : EssentiaKey.of(aspect);
            return key == null ? null : new GenericStack(key, 1);
        }
        if (storage.contents().isEmpty()) return null;
        var first = storage.contents().entries().get(0);
        var key = EssentiaKey.of(first.aspect());
        return key == null ? null : new GenericStack(key, first.amount());
    }

    @Override
    public @Nullable Slot findCarriedContext(Player player, AbstractContainerMenu menu) {
        return storage(menu.getCarried()) == null ? null : new Slot(player, menu::getCarried, menu::setCarried);
    }

    @Override
    public @Nullable Slot findPlayerSlotContext(Player player, int index) {
        var inventory = player.getInventory();
        if (storage(inventory.getItem(index)) == null) return null;
        return new Slot(player, () -> inventory.getItem(index), stack -> {
            inventory.setItem(index, stack);
            inventory.setChanged();
        });
    }

    @Override
    public long extract(Slot context, EssentiaKey what, long amount, Actionable mode) {
        return transfer(context, what, amount, mode, false);
    }

    @Override
    public long insert(Slot context, EssentiaKey what, long amount, Actionable mode) {
        return transfer(context, what, amount, mode, true);
    }

    private long transfer(Slot slot, EssentiaKey key, long amount, Actionable mode, boolean fill) {
        var holder = key.resolve(slot.player().level());
        var storage = storage(slot.get().get());
        if (amount <= 0 || holder == null || storage == null) return 0;
        int request = (int) Math.min(amount, Integer.MAX_VALUE);
        var result = fill ? storage.insert(holder, request) : storage.extract(holder, request);
        if (mode == Actionable.MODULATE && result.amountMoved() > 0) {
            var stack = result.resultingStack();
            slot.set().accept(stack);
            if (fill) {
                EssentiaTransferFeedback.playFill(slot.player(), stack, result.amountMoved());
            } else {
                EssentiaTransferFeedback.playDrain(slot.player(), stack, result.amountMoved());
            }
        }
        return result.amountMoved();
    }

    private static @Nullable IEssentiaItemStorage storage(ItemStack stack) {
        return stack.getCount() == 1 ? stack.getCapability(EssentiaCapabilities.ITEM_STORAGE).orElse(null) : null;
    }

    @Override
    public void playFillSound(Player player, EssentiaKey what) {
        // The committed replacement stack supplies the sound.
    }

    @Override
    public void playEmptySound(Player player, EssentiaKey what) {
        // The committed replacement stack supplies the sound.
    }

    @Override
    public @Nullable GenericStack getExtractableContent(Slot context) {
        return getContainedStack(context.get().get());
    }
}
