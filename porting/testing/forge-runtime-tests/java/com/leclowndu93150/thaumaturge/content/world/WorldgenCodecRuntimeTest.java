package com.leclowndu93150.thaumaturge.content.world;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.config.labyrinth.LabyrinthConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ClusteredReplaceProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ExposedSurfaceProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.StairMirrorProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.StripMarkersProcessor;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigNodeSpawnFilter;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigRarityFilter;
import com.leclowndu93150.thaumaturge.content.world.objects.MagicalCaveFloorPlacement;
import com.leclowndu93150.thaumaturge.content.world.objects.SurfaceBiomeSearch;
import com.leclowndu93150.thaumaturge.registry.TTPlacementModifiers;
import com.leclowndu93150.thaumaturge.registry.TTStructureProcessors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WorldgenCodecRuntimeTest {
    @BeforeAll
    static void registerWorldgenTypes() throws ReflectiveOperationException {
        var bus = BusBuilder.builder().build();
        TTPlacementModifiers.register(bus);
        TTStructureProcessors.register(bus);
        register(bus, Registries.PLACEMENT_MODIFIER_TYPE, BuiltInRegistries.PLACEMENT_MODIFIER_TYPE);
        register(bus, Registries.STRUCTURE_PROCESSOR, BuiltInRegistries.STRUCTURE_PROCESSOR);
    }

    @Test
    void nativePlacementDispatchKeepsTypeIdsAndNodeKinds() {
        var codec = PlacementModifier.CODEC;
        assertSame(ConfigRarityFilter.CRIMSON_PORTAL, roundTrip(codec, "{\"type\":\"thaumaturge:crimson_portal_rarity\"}"));
        assertSame(MagicalCaveFloorPlacement.INSTANCE, roundTrip(codec, "{\"type\":\"thaumaturge:magical_cave_floor\"}"));
        assertSame(SurfaceBiomeSearch.INSTANCE, roundTrip(codec, "{\"type\":\"thaumaturge:surface_biome_search\"}"));
        for (var kind : ConfigNodeSpawnFilter.Kind.values()) {
            var json = "{\"type\":\"thaumaturge:node_spawn_chance\",\"kind\":\"" + kind.getSerializedName() + "\"}";
            assertInstanceOf(ConfigNodeSpawnFilter.class, roundTrip(codec, json));
        }
    }

    @Test
    void nativeProcessorDispatchKeepsFieldsAndCanApplyDecodedProcessors() {
        var codec = StructureProcessorType.SINGLE_CODEC;
        assertSame(StairMirrorProcessor.INSTANCE, roundTrip(codec, "{\"processor_type\":\"thaumaturge:stair_mirror\"}"));
        var strip = assertInstanceOf(StripMarkersProcessor.class,
                roundTrip(codec, "{\"processor_type\":\"thaumaturge:strip_markers\"}"));
        var replacement = assertInstanceOf(ClusteredReplaceProcessor.class, roundTrip(codec, """
                {"processor_type":"thaumaturge:clustered_replace",
                 "target":{"predicate_type":"minecraft:block_match","block":"minecraft:stone"},
                 "replacements":[{"data":{"Name":"minecraft:andesite"},"weight":2}],
                 "threshold":0.0,"pitch":7,"salt":21}
                """));
        assertInstanceOf(ExposedSurfaceProcessor.class, roundTrip(codec, """
                {"processor_type":"thaumaturge:exposed_surface",
                 "target":{"predicate_type":"minecraft:block_match","block":"minecraft:stone"},
                 "floor":[{"data":{"Name":"minecraft:glowstone"},"weight":1}],
                 "density":0.5,"cluster_pitch":7,"salt":21}
                """));

        var pos = new BlockPos(5, 7, 13);
        var settings = new StructurePlaceSettings();
        var stone = new StructureTemplate.StructureBlockInfo(pos, Blocks.STONE.defaultBlockState(), null);
        var marker = new StructureTemplate.StructureBlockInfo(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState(), new CompoundTag());
        var replaced = replacement.process(null, pos, pos, stone, stone, settings, null);
        assertEquals(pos, replaced.pos());
        assertTrue(replaced.state().is(Blocks.ANDESITE));
        var stripped = strip.process(null, pos, pos, marker, marker, settings, null);
        assertTrue(stripped.state().isAir());
        assertNull(stripped.nbt());
    }

    @Test
    void nativeLabyrinthConfigKeepsEmptyDefaultsAndTheRoomAndMarkerValidators() {
        var builder = new ForgeConfigSpec.Builder();
        new LabyrinthConfig(builder);
        var spec = builder.build().getSpec();
        ForgeConfigSpec.ValueSpec rooms = spec.get("labyrinth.generation.disabledRooms");
        ForgeConfigSpec.ValueSpec markers = spec.get("labyrinth.generation.disabledMarkers");
        assertEquals(List.of(), rooms.getDefault());
        assertEquals(List.of(), markers.getDefault());
        assertTrue(rooms.test(List.of()));
        assertTrue(markers.test(List.of()));
        assertTrue(rooms.test(List.of("thaumaturge:passage/webbed_straight", "#thaumaturge:labyrinth/rare")));
        assertTrue(markers.test(List.of("thaumaturge:aura")));
        assertFalse(rooms.test(List.of("#invalid id")));
        assertFalse(markers.test(List.of("#thaumaturge:aura")));
    }

    private static <T> T roundTrip(Codec<T> codec, String json) {
        var source = JsonParser.parseString(json);
        var value = codec.parse(JsonOps.INSTANCE, source).getOrThrow(false, AssertionError::new);
        assertEquals(source, codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow(false, AssertionError::new));
        return value;
    }

    private static <T> void register(IEventBus bus, ResourceKey<Registry<T>> key, Registry<T> registry)
            throws ReflectiveOperationException {
        var event = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        event.setAccessible(true);
        ((MappedRegistry<T>) registry).unfreeze();
        try {
            bus.post(event.newInstance(key, null, registry));
        } finally {
            registry.freeze();
        }
    }
}
