package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SealsChunkData {
    public static final MapCodec<SealsChunkData> CODEC = Codecs.optionalFieldOf(SealEntity.CODEC
            .listOf(), "seals", List.of())
            .xmap(SealsChunkData::new, data -> List.copyOf(data.seals));

    private final List<SealEntity> seals;

    public SealsChunkData() {
        this.seals = new CopyOnWriteArrayList<>();
    }

    private SealsChunkData(List<SealEntity> seals) {
        this.seals = new CopyOnWriteArrayList<>(seals);
    }

    public List<SealEntity> seals() {
        return seals;
    }
}
