package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ClientboundThaumatoriumRecipesPayload(int containerId, List<Entry> entries)
        implements ModPayload {
    public static final ModPayload.Type<ClientboundThaumatoriumRecipesPayload> TYPE =
            new ModPayload.Type<>(TTIds.rl("thaumatorium_recipes"));

    public record Entry(ResourceLocation id, ItemStack output, boolean queued, AspectList aspects) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                VanillaStreamCodecs.RESOURCE_LOCATION,
                Entry::id,
                VanillaStreamCodecs.ITEM_STACK,
                Entry::output,
                ByteBufCodecs.BOOL,
                Entry::queued,
                AspectList.STREAM_CODEC,
                Entry::aspects,
                Entry::new);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundThaumatoriumRecipesPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundThaumatoriumRecipesPayload::containerId,
                    Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ClientboundThaumatoriumRecipesPayload::entries,
                    ClientboundThaumatoriumRecipesPayload::new);

    @Override
    public ModPayload.Type<ClientboundThaumatoriumRecipesPayload> type() {
        return TYPE;
    }
}
