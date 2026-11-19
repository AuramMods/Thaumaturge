package com.leclowndu93150.thaumaturge.content.entity.ranger;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class CrimsonPatrolSchedule extends SavedData {
    private static final long UNSCHEDULED = 0L;
    private static final String DATA_KEY = "data";
    private static final String NAME = "thaumaturge_crimson_patrols";

    public static final Codec<CrimsonPatrolSchedule> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(Codec.LONG.fieldOf("next_attempt").forGetter(schedule -> schedule.nextAttempt))
                    .apply(builder, CrimsonPatrolSchedule::new));

    private long nextAttempt;

    public CrimsonPatrolSchedule() {
        this(UNSCHEDULED);
    }

    private CrimsonPatrolSchedule(long nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public static CrimsonPatrolSchedule get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CrimsonPatrolSchedule::load, CrimsonPatrolSchedule::new, NAME);
    }

    private static CrimsonPatrolSchedule load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get(DATA_KEY)).result().orElseGet(CrimsonPatrolSchedule::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put(DATA_KEY, Codecs.getOrThrow(CODEC.encodeStart(NbtOps.INSTANCE, this)));
        return tag;
    }

    public boolean isScheduled() {
        return nextAttempt != UNSCHEDULED;
    }

    public boolean isDue(long gameTime) {
        return gameTime >= nextAttempt;
    }

    public void schedule(long gameTime) {
        nextAttempt = gameTime;
        setDirty();
    }
}
