package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryView;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;

public record ClientboundReliquaryViewPayload(BlockPos pos, ReliquaryView view) implements ModPayload {
    public static final Type<ClientboundReliquaryViewPayload> TYPE = new Type<>(TTIds.rl("reliquary_view"));
    public static final StreamCodec<ByteBuf, ClientboundReliquaryViewPayload> STREAM_CODEC = StreamCodec.composite(
            VanillaStreamCodecs.BLOCK_POS,
            ClientboundReliquaryViewPayload::pos,
            ReliquaryView.STREAM_CODEC,
            ClientboundReliquaryViewPayload::view,
            ClientboundReliquaryViewPayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
