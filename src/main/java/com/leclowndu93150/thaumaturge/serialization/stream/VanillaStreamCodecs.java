package com.leclowndu93150.thaumaturge.serialization.stream;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Vanilla values encoded through their 1.20.1 network methods. */
public final class VanillaStreamCodecs {
    public static final StreamCodec<ByteBuf, ResourceLocation> RESOURCE_LOCATION = StreamCodec.of(
            (buffer, value) -> new FriendlyByteBuf(buffer).writeResourceLocation(value),
            buffer -> new FriendlyByteBuf(buffer).readResourceLocation());
    public static final StreamCodec<ByteBuf, UUID> UUID = StreamCodec.of(
            (buffer, value) -> new FriendlyByteBuf(buffer).writeUUID(value),
            buffer -> new FriendlyByteBuf(buffer).readUUID());
    public static final StreamCodec<ByteBuf, BlockPos> BLOCK_POS = StreamCodec.of(
            (buffer, value) -> new FriendlyByteBuf(buffer).writeBlockPos(value),
            buffer -> new FriendlyByteBuf(buffer).readBlockPos());
    public static final StreamCodec<ByteBuf, Direction> DIRECTION = ByteBufCodecs.idMapper(
            id -> id >= 0 && id < Direction.values().length ? Direction.values()[id] : null, Direction::ordinal);
    public static final StreamCodec<ByteBuf, GlobalPos> GLOBAL_POS = StreamCodec.of((buffer, value) -> {
        RESOURCE_LOCATION.encode(buffer, value.dimension().location());
        BLOCK_POS.encode(buffer, value.pos());
    }, buffer -> {
        var dimension = ResourceKey.create(Registries.DIMENSION, RESOURCE_LOCATION.decode(buffer));
        return GlobalPos.of(dimension, BLOCK_POS.decode(buffer));
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> ITEM_STACK = StreamCodec.of(
            (buffer, value) -> buffer.writeItem(value), FriendlyByteBuf::readItem);
    public static final StreamCodec<RegistryFriendlyByteBuf, Ingredient> INGREDIENT = StreamCodec.of(
            (buffer, value) -> value.toNetwork(buffer), Ingredient::fromNetwork);

    private VanillaStreamCodecs() {}

    public static <V> StreamCodec<ByteBuf, ResourceKey<V>> resourceKey(ResourceKey<? extends Registry<V>> registry) {
        return RESOURCE_LOCATION.map(id -> ResourceKey.create(registry, id), ResourceKey::location);
    }
}
