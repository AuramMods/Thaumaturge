package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.content.blockentity.BlockCapabilityAccess;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

/** Connects AE2's ordinary buses to Thaumaturge's essentia sides. */
final class EssentiaBusAccess implements StackImportStrategy, StackExportStrategy, ExternalStorageStrategy {
    private static final int IMPORT_PULL = 128;
    // During transfer, the device being moved to or from cannot also be an ME storage destination.
    private static final ThreadLocal<EssentiaBusAccess> TRANSFERRING = new ThreadLocal<>();

    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction face;

    EssentiaBusAccess(ServerLevel level, BlockPos pos, Direction face) {
        this.level = level;
        this.pos = pos.immutable();
        this.face = face;
    }

    static void register() {
        var type = EssentiaKeyType.INSTANCE;
        StackImportStrategy.register(type, EssentiaBusAccess::new);
        StackExportStrategy.register(type, EssentiaBusAccess::new);
        ExternalStorageStrategy.register(type, EssentiaBusAccess::new);
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.hasOperationsLeft() || !context.isKeyTypeEnabled(EssentiaKeyType.INSTANCE)) return false;
        var conduit = conduit();
        if (conduit == null || !conduit.releasesOn(face) || conduit.pull(face).strength() >= IMPORT_PULL
                || conduit.pullThreshold() > IMPORT_PULL) return false;
        var aspect = conduit.heldAspect(face);
        if (aspect == null || conduit.heldAmount(face) <= 0) return false;
        var key = EssentiaKey.of(aspect);
        if (key == null || context.isInFilter(key) == context.isInverted()) return false;

        var network = context.getInternalStorage().getInventory();
        var source = context.getActionSource();
        var previous = TRANSFERRING.get();
        TRANSFERRING.set(this);
        try {
            int wanted = Math.min(context.getOperationsRemaining(), conduit.heldAmount(face));
            int available = conduit.extract(aspect, wanted, face, true);
            int capacity = (int) StorageHelper.poweredInsert(
                    context.getEnergySource(), network, key, available, source, Actionable.SIMULATE);
            if (capacity <= 0) return false;

            int extracted = conduit.extract(aspect, capacity, face, false);
            if (extracted <= 0) return false;
            long inserted = StorageHelper.poweredInsert(context.getEnergySource(), network, key, extracted, source);
            if (inserted < extracted) {
                int remainder = (int) (extracted - inserted);
                remainder -= conduit.insert(aspect, remainder, face, false);
                key.dropAt(level, pos, remainder);
            }
            context.reduceOperationsRemaining(inserted);
            return !context.hasOperationsLeft();
        } finally {
            TRANSFERRING.set(previous);
        }
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long maxAmount) {
        if (!(what instanceof EssentiaKey key) || maxAmount <= 0) return 0;
        var network = context.getInternalStorage().getInventory();
        var source = context.getActionSource();
        var previous = TRANSFERRING.get();
        TRANSFERRING.set(this);
        try {
            long available = StorageHelper.poweredExtraction(
                    context.getEnergySource(), network, key, maxAmount, source, Actionable.SIMULATE);
            long accepted = push(key, available, Actionable.SIMULATE);
            if (accepted <= 0) return 0;

            long extracted = StorageHelper.poweredExtraction(context.getEnergySource(), network, key, accepted, source);
            long inserted = push(key, extracted, Actionable.MODULATE);
            long remainder = extracted - inserted;
            if (remainder > 0) {
                remainder -= network.insert(key, remainder, Actionable.MODULATE, source);
                key.dropAt(level, pos, remainder);
            }
            return inserted;
        } finally {
            TRANSFERRING.set(previous);
        }
    }

    @Override
    public long push(AEKey what, long maxAmount, Actionable mode) {
        if (!(what instanceof EssentiaKey key) || maxAmount <= 0) return 0;
        var aspect = key.resolve(level);
        var conduit = conduit();
        if (aspect == null || conduit == null || !conduit.receivesOn(face)) return 0;
        var pull = conduit.pull(face);
        if (pull.isActive() && !pull.accepts(aspect)) return 0;
        return conduit.insert(aspect, (int) Math.min(maxAmount, Integer.MAX_VALUE), face, mode.isSimulate());
    }

    @Override
    public @Nullable MEStorage createWrapper(boolean extractableOnly, Runnable changed) {
        return storage() == null ? null : new ExternalEssentiaStorage(extractableOnly, changed);
    }

    private @Nullable IEssentiaConduit conduit() {
        if (!LoadedChunks.contains(level, pos)) return null;
        var conduit = EssentiaAccess.conduit(level, pos, face);
        return conduit != null && conduit.connectsOn(face) ? conduit : null;
    }

    private @Nullable IEssentiaStorage storage() {
        if (!LoadedChunks.contains(level, pos)) return null;
        return BlockCapabilityAccess.find(level, pos, EssentiaCapabilities.STORAGE, face);
    }

    private boolean transferringHere() {
        var transfer = TRANSFERRING.get();
        return transfer != null && transfer.level == level && transfer.pos.equals(pos);
    }

    private final class ExternalEssentiaStorage implements MEStorage {
        private final boolean extractableOnly;
        private final Runnable changed;

        private ExternalEssentiaStorage(boolean extractableOnly, Runnable changed) {
            this.extractableOnly = extractableOnly;
            this.changed = changed;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (transferringHere() || !(what instanceof EssentiaKey key) || amount <= 0) return 0;
            var aspect = key.resolve(level);
            var storage = storage();
            if (storage == null || aspect == null) return 0;
            int inserted = storage.insert(aspect, (int) Math.min(amount, Integer.MAX_VALUE), mode.isSimulate());
            if (inserted > 0 && !mode.isSimulate()) changed.run();
            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (transferringHere() || !(what instanceof EssentiaKey key) || amount <= 0) return 0;
            var aspect = key.resolve(level);
            var storage = storage();
            if (storage == null || aspect == null) return 0;
            int extracted = storage.extract(aspect, (int) Math.min(amount, Integer.MAX_VALUE), mode.isSimulate());
            if (extracted > 0 && !mode.isSimulate()) changed.run();
            return extracted;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            if (transferringHere()) return;
            var storage = storage();
            if (storage == null) return;
            for (var entry : storage.contents().entries()) {
                var key = EssentiaKey.of(entry.aspect());
                if (key == null) continue;
                int amount = extractableOnly
                        ? storage.extract(entry.aspect(), entry.amount(), true)
                        : entry.amount();
                if (amount > 0) out.add(key, amount);
            }
        }

        @Override
        public Component getDescription() {
            return Component.translatable(level.getBlockState(pos).getBlock().getDescriptionId());
        }
    }
}
