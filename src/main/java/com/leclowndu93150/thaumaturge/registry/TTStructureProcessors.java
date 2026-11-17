package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ClusteredReplaceProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ExposedSurfaceProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.StairMirrorProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.StripMarkersProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTStructureProcessors {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, TTIds.MODID);

    public static final RegistryObject<StructureProcessorType<StripMarkersProcessor>>
            STRIP_MARKERS = PROCESSORS.register("strip_markers", () -> () -> StripMarkersProcessor.CODEC);
    public static final RegistryObject<StructureProcessorType<ExposedSurfaceProcessor>>
            EXPOSED_SURFACE = PROCESSORS.register("exposed_surface", () -> () -> ExposedSurfaceProcessor.CODEC);
    public static final RegistryObject<StructureProcessorType<ClusteredReplaceProcessor>>
            CLUSTERED_REPLACE = PROCESSORS.register("clustered_replace", () -> () -> ClusteredReplaceProcessor.CODEC);
    public static final RegistryObject<StructureProcessorType<StairMirrorProcessor>>
            STAIR_MIRROR = PROCESSORS.register("stair_mirror", () -> () -> StairMirrorProcessor.CODEC);

    private TTStructureProcessors() {}

    public static void register(IEventBus modBus) {
        PROCESSORS.register(modBus);
    }
}
