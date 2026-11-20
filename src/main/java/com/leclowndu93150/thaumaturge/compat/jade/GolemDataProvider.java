package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public enum GolemDataProvider implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    static final String RANK_XP = "RankXp";
    static final String RANK_XP_REQUIRED = "RankXpRequired";
    private static final ResourceLocation UID = TTIds.rl("golem");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (!accessor.showDetails()
                || !(accessor.getEntity() instanceof EntityThaumaturgeGolem golem)
                || !golem.properties().hasTrait(TTGolemTraits.SMART.get())) return;
        int rank = golem.properties().rank();
        if (rank >= EntityThaumaturgeGolem.MAX_RANK) return;
        tag.putInt(RANK_XP, golem.getRankXp());
        tag.putInt(RANK_XP_REQUIRED, EntityThaumaturgeGolem.xpForNextRank(rank));
    }
}
