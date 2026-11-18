package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SocketedFocus(ItemStack focus) {
    public SocketedFocus {
        if (focus == null) focus = ItemStack.EMPTY;
    }

    public static final Codec<SocketedFocus> CODEC = RecordCodecBuilder.create(
            i -> i.group(ItemStackCodecs.CODEC.fieldOf("focus").forGetter(SocketedFocus::focus))
                    .apply(i, SocketedFocus::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SocketedFocus> STREAM_CODEC =
            StreamCodec.composite(VanillaStreamCodecs.ITEM_STACK, SocketedFocus::focus, SocketedFocus::new);
}
