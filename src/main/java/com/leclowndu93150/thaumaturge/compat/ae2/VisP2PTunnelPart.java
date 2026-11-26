package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.leclowndu93150.thaumaturge.api.transaction.RootCommitJournal;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.blockentity.BlockCapabilityAccess;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

final class VisP2PTunnelPart extends P2PTunnelPart<VisP2PTunnelPart> {
    static final ResourceLocation MODEL = TTIds.rl("part/vis_p2p_tunnel");
    private static final P2PModels MODELS = new P2PModels(MODEL);

    VisP2PTunnelPart(IPartItem<?> item) {
        super(item);
    }

    @Nullable IVisRelaySource source() {
        if (!isOutput() || !isActive()) return null;
        var input = getInput();
        if (input == null || !input.isActive() || !(input.getLevel() instanceof ServerLevel level)) return null;
        var pos = input.getBlockEntity().getBlockPos().relative(input.getSide());
        if (!LoadedChunks.contains(level, pos)) return null;
        if (level.getBlockEntity(pos) instanceof BlockEntityVisRelay relay) {
            var source = relay.resolveSource(level);
            return source == null ? null : source.source();
        }
        return BlockCapabilityAccess.find(level, pos, VisRelayCapabilities.SOURCE, null);
    }

    int drain(IVisRelaySource source, ResourceKey<IAspect> aspect, int amount, TransactionContext transaction) {
        int drained = source.drainCentivis(aspect, amount, transaction);
        if (drained > 0) {
            new RootCommitJournal(() -> deductEnergyCost(drained / 100.0, PowerUnits.AE)).updateSnapshots(transaction);
        }
        return drained;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(isPowered(), isActive());
    }
}
