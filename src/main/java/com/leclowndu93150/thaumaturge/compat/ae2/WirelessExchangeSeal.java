package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.prioritylist.IPartitionList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.blockentity.BlockCapabilityAccess;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import org.jspecify.annotations.Nullable;

/** Routes seal work through the golem's wireless link using the same strategies as ME buses. */
final class WirelessExchangeSeal implements ISealBehavior {
    private final boolean export;
    private @Nullable Task pending;

    WirelessExchangeSeal(boolean export) {
        this.export = export;
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (seal.isStoppedByRedstone(level) || (level.getGameTime() + seal.pos().pos().asLong()) % 20 != 0
                || pending != null && !pending.isEnded() && !pending.isCompleted()) return;
        pending = Task.atBlock(seal.pos(), seal.pos().pos());
        pending.setPriority(seal.priority());
        GolemDispatch.postTask(level, pending);
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        if (!(golem.level() instanceof ServerLevel level) || seal.owner() == null
                || !golem.ownerIdentity().filter(seal.owner()::equals).isPresent()) return false;
        var link = GolemWirelessBackpack.link(golem);
        if (link == null || !LoadedChunks.contains(level, seal.pos().pos()) || seal.isStoppedByRedstone(level)) return false;
        var key = key(level, seal);
        if (export) {
            if (key == null || link.grid().getStorageService().getCachedInventory().get(key) <= 0) return false;
            var access = StackWorldBehaviors.createExportFacade(level, seal.pos().pos(), seal.pos().face());
            return access.push(key, key.getAmountPerOperation(), Actionable.SIMULATE) > 0;
        }
        return hasImport(level, seal.pos(), key);
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (canPerform(seal, golem, task)) {
            var link = GolemWirelessBackpack.link(golem);
            if (link != null) {
                var key = key(level, seal);
                var context = new SealTransfer(link, key);
                var pos = seal.pos().pos();
                var face = seal.pos().face();
                if (export && key != null) {
                    var access = StackWorldBehaviors.createExportFacade(level, pos, face);
                    if (access.transfer(context, key, key.getAmountPerOperation()) > 0) golem.animateSwing();
                } else if (!export) {
                    StackWorldBehaviors.createImportFacade(level, pos, face).transfer(context);
                    if (context.hasDoneWork()) golem.animateSwing();
                }
            }
        }
        task.end();
        return true;
    }

    private static @Nullable AEKey key(ServerLevel level, ISealEntity seal) {
        ItemStack filter = seal.filter().orElseThrow().stack(0);
        if (filter.isEmpty()) return null;

        var pos = seal.pos();
        if (EssentiaAccess.hasConduit(level, pos.pos(), pos.face())) {
            var crystal = EssentiaCrystalAccess.aspect(filter);
            if (crystal != null) return EssentiaKey.of(crystal);
            var vessel = filter.getCapability(EssentiaCapabilities.ITEM_STORAGE).orElse(null);
            if (vessel != null && !vessel.contents().isEmpty()) {
                return EssentiaKey.of(vessel.contents().entries().get(0).aspect());
            }
        }
        if (BlockCapabilityAccess.find(level, pos.pos(), ForgeCapabilities.FLUID_HANDLER, pos.face()) != null) {
            var fluid = FluidUtil.getFluidContained(filter).map(AEFluidKey::of).orElse(null);
            if (fluid != null) return fluid;
        }
        return AEItemKey.of(filter);
    }

    private static boolean hasImport(ServerLevel level, SealPos pos, @Nullable AEKey filter) {
        if (filter == null || filter instanceof EssentiaKey) {
            var conduit = EssentiaAccess.conduit(level, pos.pos(), pos.face());
            if (conduit != null && conduit.releasesOn(pos.face())) {
                var aspect = conduit.heldAspect(pos.face());
                if (aspect != null && (filter == null || filter.equals(EssentiaKey.of(aspect)))) return true;
            }
        }
        if (filter == null || filter instanceof AEFluidKey) {
            var tank = BlockCapabilityAccess.find(level, pos.pos(), ForgeCapabilities.FLUID_HANDLER, pos.face());
            if (tank != null) {
                for (int i = 0; i < tank.getTanks(); i++) {
                    var fluid = tank.getFluidInTank(i);
                    if (!fluid.isEmpty() && (filter == null || filter.equals(AEFluidKey.of(fluid)))) return true;
                }
            }
        }
        if (filter == null || filter instanceof AEItemKey) {
            var inv = BlockCapabilityAccess.find(level, pos.pos(), ForgeCapabilities.ITEM_HANDLER, pos.face());
            if (inv != null) {
                for (int i = 0; i < inv.getSlots(); i++) {
                    var stack = inv.getStackInSlot(i);
                    if (!stack.isEmpty() && (filter == null || filter.equals(AEItemKey.of(stack)))) return true;
                }
            }
        }
        return false;
    }

    private static final class SealTransfer implements StackTransferContext {
        private final GolemWirelessBackpack.Link link;
        private final @Nullable AEKey key;
        private final IPartitionList filter;
        private int operations = 1;
        private boolean inverted;

        SealTransfer(GolemWirelessBackpack.Link link, @Nullable AEKey key) {
            this.link = link;
            this.key = key;
            var builder = IPartitionList.builder();
            if (key != null) builder.add(key);
            filter = builder.build();
        }

        @Override public IStorageService getInternalStorage() { return link.grid().getStorageService(); }
        @Override public IEnergySource getEnergySource() { return link.grid().getEnergyService(); }
        @Override public IActionSource getActionSource() { return link.source(); }
        @Override public int getOperationsRemaining() { return operations; }
        @Override public void setOperationsRemaining(int remaining) { operations = remaining; }
        @Override public boolean hasOperationsLeft() { return operations > 0; }
        @Override public boolean hasDoneWork() { return operations < 1; }
        @Override public boolean isKeyTypeEnabled(AEKeyType type) { return key == null || key.getType() == type; }
        @Override public boolean isInFilter(AEKey candidate) { return key == null || key.equals(candidate); }
        @Override public IPartitionList getFilter() { return filter; }
        @Override public void setInverted(boolean inverted) { this.inverted = inverted; }
        @Override public boolean isInverted() { return key != null && inverted; }
        @Override public void reduceOperationsRemaining(long used) { operations -= (int) Math.min(operations, used); }

        @Override
        public boolean canInsert(AEItemKey candidate, long amount) {
            return getInternalStorage().getInventory().insert(candidate, amount, Actionable.SIMULATE, getActionSource()) > 0;
        }
    }
}
