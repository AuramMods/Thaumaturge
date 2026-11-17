package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;
import org.jspecify.annotations.Nullable;

public record ServerboundTableCombinePayload(
        BlockPos pos, ResourceLocation first, ResourceLocation second, boolean bonusFirst, boolean bonusSecond)
        implements ModPayload {
    public static final ModPayload.Type<ServerboundTableCombinePayload> TYPE =
            new ModPayload.Type<>(TTIds.rl("table_combine"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTableCombinePayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ServerboundTableCombinePayload::pos,
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    ServerboundTableCombinePayload::first,
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    ServerboundTableCombinePayload::second,
                    ByteBufCodecs.BOOL,
                    ServerboundTableCombinePayload::bonusFirst,
                    ByteBufCodecs.BOOL,
                    ServerboundTableCombinePayload::bonusSecond,
                    ServerboundTableCombinePayload::new);

    public static void handle(ServerboundTableCombinePayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntityResearchTable table = BlockMenuGuard.target(context, payload.pos(), MenuResearchTable.class);
            if (table == null) {
                return;
            }
            ServerPlayer player = (ServerPlayer) context.player();
            Holder<IAspect> first = resolve(player, payload.first());
            Holder<IAspect> second = resolve(player, payload.second());
            if (first != null && second != null) {
                table.combineAspects(player, first, second, payload.bonusFirst(), payload.bonusSecond());
            }
        });
    }

    private static @Nullable Holder<IAspect> resolve(ServerPlayer player, ResourceLocation id) {
        return player.level().registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(ResourceKey.create(IAspect.REGISTRY_KEY, id))
                .map(reference -> (Holder<IAspect>) reference)
                .orElse(null);
    }

    @Override
    public ModPayload.Type<ServerboundTableCombinePayload> type() {
        return TYPE;
    }
}
