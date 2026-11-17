package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.FocusCaster;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundFocusChangePayload(String focusKey) implements ModPayload {
    public static final Type<ServerboundFocusChangePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "focus_change"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFocusChangePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ServerboundFocusChangePayload::focusKey,
                    ServerboundFocusChangePayload::new);

    public static void handle(ServerboundFocusChangePayload payload, PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack main = player.getMainHandItem();
            if (main.getItem() instanceof FocusCaster) {
                CasterManager.changeFocus(main, player.level(), player, payload.focusKey());
                return;
            }
            ItemStack off = player.getOffhandItem();
            if (off.getItem() instanceof FocusCaster) {
                CasterManager.changeFocus(off, player.level(), player, payload.focusKey());
            }
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
