package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/** ME storage identity for an aspect, independent of world and datapack lifetimes. */
public final class EssentiaKey extends AEKey {
    private final ResourceKey<IAspect> aspect;

    public EssentiaKey(ResourceLocation id) {
        this(ResourceKey.create(IAspect.REGISTRY_KEY, id));
    }

    private EssentiaKey(ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
    }

    public static @Nullable EssentiaKey of(Holder<IAspect> aspect) {
        return aspect.unwrapKey().map(EssentiaKey::new).orElse(null);
    }

    public @Nullable Holder<IAspect> resolve(@Nullable Level level) {
        return Aspects.resolve(level, aspect);
    }

    @Override
    public AEKeyType getType() {
        return EssentiaKeyType.INSTANCE;
    }

    @Override
    public EssentiaKey dropSecondary() {
        return this;
    }

    @Override
    public CompoundTag toTag() {
        var tag = new CompoundTag();
        tag.putString("id", getId().toString());
        return tag;
    }

    @Override
    public Object getPrimaryKey() {
        return aspect;
    }

    @Override
    public ResourceLocation getId() {
        return aspect.location();
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeResourceLocation(getId());
    }

    @Override
    protected Component computeDisplayName() {
        ResourceLocation id = getId();
        return Component.translatableWithFallback(
                "aspect." + id.getNamespace() + "." + id.getPath(), id.toString());
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        var holder = resolve(level);
        if (holder == null || amount <= 0) return;
        var jar = new ItemStack(TTItems.JAR_NORMAL.get());
        var storage = jar.getCapability(EssentiaCapabilities.ITEM_STORAGE).orElseThrow(IllegalStateException::new);
        while (amount > 0) {
            var result = storage.insert(holder, (int) Math.min(amount, Integer.MAX_VALUE));
            if (result.amountMoved() <= 0) break;
            drops.add(result.resultingStack());
            amount -= result.amountMoved();
        }
    }

    void dropAt(ServerLevel level, BlockPos pos, long amount) {
        if (amount <= 0) return;
        var drops = new ArrayList<ItemStack>();
        addDrops(amount, drops, level, pos);
        drops.forEach(stack -> Block.popResource(level, pos, stack));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EssentiaKey key && aspect == key.aspect;
    }

    @Override
    public int hashCode() {
        return aspect.hashCode();
    }
}
