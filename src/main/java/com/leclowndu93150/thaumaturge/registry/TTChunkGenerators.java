package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world.OuterLandsChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTChunkGenerators {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, TTIds.MODID);

    public static final RegistryObject<MapCodec<OuterLandsChunkGenerator>>
            OUTER_LANDS = CHUNK_GENERATORS.register("outer_lands", () -> OuterLandsChunkGenerator.CODEC);

    private TTChunkGenerators() {}

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
