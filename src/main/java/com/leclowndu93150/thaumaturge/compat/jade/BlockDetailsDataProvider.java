package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaCommonRegistration;

public enum BlockDetailsDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID = TTIds.rl("block_details");
    public static final String DATA = "ThaumaturgeDetails";

    static void register(IWailaCommonRegistration registration) {
        List<Class<? extends BlockEntity>> types = JadeBlockProviderHolder.HANDLERS.stream()
                .<Class<? extends BlockEntity>>map(JadeBlockHandler::type)
                .distinct()
                .toList();
        for (Class<? extends BlockEntity> type : types) {
            // Jade looks up every superclass. One provider selects the matching handler in the configured order.
            if (types.stream().noneMatch(parent -> parent != type && parent.isAssignableFrom(type))) {
                registration.registerBlockDataProvider(INSTANCE, type);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BlockEntity entity = JadeBlockProviderHolder.resolve(accessor);
        if (entity == null) return;
        for (JadeBlockHandler<?> handler : JadeBlockProviderHolder.HANDLERS) {
            if (handler.type().isInstance(entity)) {
                TTNbt.store(tag, DATA, JadeBlockDetails.CODEC, handler.read(entity, accessor.getPlayer()));
                return;
            }
        }
    }
}
