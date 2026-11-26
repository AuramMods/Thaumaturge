package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.stacks.AEKeyType;
import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class EssentiaKeyType extends AEKeyType {
    public static final EssentiaKeyType INSTANCE = new EssentiaKeyType();

    private EssentiaKeyType() {
        super(TTIds.rl("essentia"), EssentiaKey.class, Component.translatable("gui.thaumaturge.essentia"));
    }

    @Override
    public EssentiaKey readFromPacket(FriendlyByteBuf input) {
        return new EssentiaKey(input.readResourceLocation());
    }

    @Override
    public @Nullable EssentiaKey loadKeyFromTag(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        return id == null || id.getPath().isEmpty() ? null : new EssentiaKey(id);
    }

    @Override
    public int getAmountPerByte() {
        return 8;
    }
}
