package com.leclowndu93150.thaumaturge.content.golem.tasks;

import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.SupplyOrder;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** The route for a supply order whose items have already been picked up by this golem. */
public record GolemDelivery(ResourceKey<Level> dimension, SealPos provider, Either<SealPos, UUID> destination,
                            ItemStack wanted, int batch, byte priority) {
    private static final Codec<ItemStack> STACK_CODEC = CompoundTag.CODEC.xmap(
            ItemStack::of, stack -> stack.save(new CompoundTag()));

    public static final Codec<GolemDelivery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(GolemDelivery::dimension),
                    SealPos.CODEC.fieldOf("provider").forGetter(GolemDelivery::provider),
                    Codec.either(SealPos.CODEC, UUIDUtil.CODEC).fieldOf("destination").forGetter(GolemDelivery::destination),
                    STACK_CODEC.fieldOf("wanted").forGetter(GolemDelivery::wanted),
                    Codec.INT.fieldOf("batch").forGetter(GolemDelivery::batch),
                    Codec.BYTE.fieldOf("priority").forGetter(GolemDelivery::priority))
            .apply(instance, GolemDelivery::new));

    public static GolemDelivery of(ServerLevel level, SealPos provider, SupplyOrder order, byte priority) {
        Entity recipient = order.recipient();
        Either<SealPos, UUID> destination = recipient == null
                ? Either.left(new SealPos(order.destinationBlock(), order.destinationFace()))
                : Either.right(recipient.getUUID());
        return new GolemDelivery(level.dimension(), provider, destination, order.wanted().copy(), order.batch(), priority);
    }

    public boolean matches(Level level, @Nullable SealPos origin, SupplyOrder order) {
        return dimension.equals(level.dimension()) && provider.equals(origin) && batch == order.batch()
                && wanted.getCount() == order.wanted().getCount()
                && ItemStack.isSameItemSameTags(wanted, order.wanted())
                && destination.map(block -> block.pos().equals(order.destinationBlock())
                                && block.face() == order.destinationFace(),
                        uuid -> order.recipient() != null && uuid.equals(order.recipient().getUUID()));
    }

    public Optional<SupplyOrder> restoreOrder(ServerLevel level) {
        return destination.map(block -> {
            if (!LoadedChunks.contains(level, block.pos())) {
                return Optional.empty();
            }
            return GolemDispatch.orderSupply(level, block.pos(), block.face(), wanted, batch);
        }, recipient -> {
            Entity target = level.getEntity(recipient);
            if (target == null || !target.isAlive()) {
                return Optional.empty();
            }
            return GolemDispatch.orderSupply(level, target, wanted, batch);
        });
    }
}
