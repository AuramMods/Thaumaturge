package com.leclowndu93150.thaumaturge.content.world;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeLocationIndex;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthData;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthTuning;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeLandmarks;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazePlan;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeState;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.PendingTrigger;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.entity.ranger.CrimsonPatrolSchedule;
import com.leclowndu93150.thaumaturge.content.research.link.ResearchLinkData;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintOriginRegistry;
import com.mojang.serialization.Lifecycle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeSavedDataRuntimeTest {
    private static final ResourceKey<Registry<String>> VALUES = ResourceKey.createRegistryKey(id("values"));
    private static final ResourceKey<String> VALUE = ResourceKey.create(VALUES, id("fixture"));
    private static final LabyrinthMarkerType<RegistryMarker> MARKER_TYPE = new LabyrinthMarkerType<>(
            RegistryFixedCodec.create(VALUES).fieldOf("value").xmap(RegistryMarker::new, RegistryMarker::value));

    @Test
    void taintOriginsAndPatrolSchedulesStaySeparateAcrossWorldSaveAndReload(@TempDir Path root) throws Exception {
        var firstDirectory = Files.createDirectory(root.resolve("first"));
        var secondDirectory = Files.createDirectory(root.resolve("second"));
        var level = level(firstDirectory);
        var seeds = TaintOriginRegistry.get(level);
        var first = new BlockPos(-26, 61, 21);
        var moved = new BlockPos(17, 59, -12);
        seeds.add(first);
        seeds.move(first, moved);
        var patrol = CrimsonPatrolSchedule.get(level);
        assertFalse(patrol.isScheduled());
        patrol.schedule(8100);
        level.getDataStorage().save();
        assertFalse(seeds.isDirty());
        assertFalse(patrol.isDirty());
        assertTrue(Files.isRegularFile(firstDirectory.resolve("thaumaturge_taint_seeds.dat")));
        assertTrue(Files.isRegularFile(firstDirectory.resolve("thaumaturge_crimson_patrols.dat")));

        var reloaded = level(firstDirectory);
        assertNull(TaintOriginRegistry.get(reloaded).inChunk(-2, 1));
        assertEquals(List.of(moved), TaintOriginRegistry.get(reloaded).inChunk(1, -1));
        assertFalse(CrimsonPatrolSchedule.get(reloaded).isDue(8099));
        assertTrue(CrimsonPatrolSchedule.get(reloaded).isDue(8100));
        var second = level(secondDirectory);
        assertNull(TaintOriginRegistry.get(second).inChunk(1, -1));
        assertFalse(CrimsonPatrolSchedule.get(second).isScheduled());
    }

    @Test
    void nativeStorageReadsAndRewritesTheLegacyNodeIndex(@TempDir Path directory) throws Exception {
        var position = new BlockPos(17, 65, -30);
        var node = new CompoundTag();
        node.putLong("Pos", position.asLong());
        node.putString("Type", "pure");
        var nodes = new ListTag();
        nodes.add(node);
        var payload = new CompoundTag();
        payload.put("Nodes", nodes);
        payload.putBoolean("LegacyMigrationComplete", true);
        var root = new CompoundTag();
        root.put("data", payload);
        root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var file = directory.resolve("thaumaturge_node_locations.dat").toFile();
        NbtIo.writeCompressed(root, file);

        var level = level(directory);
        var index = NodeLocationIndex.get(level);
        assertEquals(Optional.of(position), index.findNearest(BlockPos.ZERO, NodeType.PURE));
        assertTrue(index.isDirty());
        level.getDataStorage().save();
        var rewritten = NbtIo.readCompressed(file).getCompound("data");
        assertFalse(rewritten.contains("Nodes"));
        assertFalse(rewritten.contains("LegacyMigrationComplete"));
        assertEquals("pure", rewritten.getList("nodes", Tag.TAG_COMPOUND).getCompound(0).getString("type"));
        assertEquals(Optional.of(position), NodeLocationIndex.get(level(directory)).findNearest(BlockPos.ZERO, NodeType.PURE));
    }

    @Test
    void researchShareKeepsItsOverworldFileAndSharedProgress(@TempDir Path directory) {
        var server = mock(MinecraftServer.class);
        var first = new UUID(1L, 2L);
        var second = new UUID(3L, 4L);
        var level = level(directory);
        when(server.overworld()).thenReturn(level);
        var progress = new ResearchLinkData.Progress(3, true);
        ResearchLinkData.get(server).link(first, second).progress().put(id("research"), progress);
        level.getDataStorage().save();
        assertTrue(Files.isRegularFile(directory.resolve("research_share.dat")));
        var reloaded = level(directory);
        when(server.overworld()).thenReturn(reloaded);
        assertEquals(progress, ResearchLinkData.get(server).linkBetween(second, first).progress().get(id("research")));
    }

    @Test
    void labyrinthSaveAndLoadUseTheOwningWorldRegistriesForMarkerPayloads(@TempDir Path directory) {
        var types = new MappedRegistry<LabyrinthMarkerType<?>>(LabyrinthMarkerType.REGISTRY_KEY, Lifecycle.stable());
        types.register(ResourceKey.create(LabyrinthMarkerType.REGISTRY_KEY, id("marker")), MARKER_TYPE, Lifecycle.stable());
        types.freeze();
        var bindings = mock(LabyrinthHelper.Bindings.class);
        when(bindings.markerTypes()).thenReturn(types);
        LabyrinthHelper.bind(bindings);

        var first = level(directory);
        var firstAccess = registries("first owner");
        when(first.registryAccess()).thenReturn(firstAccess);
        var mazeId = new MazeId(7);
        var plan = new MazePlan(mazeId, 2, GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO),
                new MazeGeometry(new ChunkPos(10, 20), 1, 1, 42, 16, 44), 998L,
                ResourceKey.create(LabyrinthDefinition.REGISTRY_KEY, id("maze")), Optional.empty(),
                LabyrinthTuning.DEFAULT, List.of(), List.of(), new long[0], new int[1],
                new MazeLandmarks(Map.of(), List.of(), List.of(), new BoundingBox(0, 40, 0, 15, 56, 15)));
        var marker = new RegistryMarker(firstAccess.registryOrThrow(VALUES).getHolderOrThrow(VALUE));
        var trigger = new PendingTrigger(0, BlockPos.ZERO, 5, 0, marker);
        LabyrinthData.get(first).commit(new MazeRecord(plan, MazeState.fresh(900L, List.of(trigger))));
        first.getDataStorage().save();
        assertTrue(Files.isRegularFile(directory.resolve("thaumaturge_labyrinths.dat")));

        var second = level(directory);
        var secondAccess = registries("second owner");
        when(second.registryAccess()).thenReturn(secondAccess);
        var reloaded = LabyrinthData.get(second);
        assertEquals(new MazeId(8), reloaded.nextId());
        assertEquals(1, reloaded.nextRegion());
        var record = reloaded.get(mazeId).orElseThrow();
        assertEquals(2, record.plan().generation());
        assertEquals(998L, record.plan().seed());
        assertEquals(900L, record.state().createdAt());
        var reloadedMarker = assertInstanceOf(RegistryMarker.class, record.state().triggers().get(0).marker());
        assertSame(secondAccess.registryOrThrow(VALUES).getHolderOrThrow(VALUE), reloadedMarker.value());
    }

    private static ServerLevel level(Path directory) {
        var level = mock(ServerLevel.class);
        when(level.getDataStorage()).thenReturn(new DimensionDataStorage(directory.toFile(), DataFixers.getDataFixer()));
        return level;
    }

    private static RegistryAccess registries(String value) {
        var entries = new MappedRegistry<String>(VALUES, Lifecycle.stable());
        entries.register(VALUE, value, Lifecycle.stable());
        return new RegistryAccess.ImmutableRegistryAccess(List.of(entries)).freeze();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("port_saved_data", path);
    }

    private record RegistryMarker(Holder<String> value) implements LabyrinthMarker {
        @Override
        public LabyrinthMarkerType<?> type() {
            return MARKER_TYPE;
        }

        @Override
        public MarkerPhase phase() {
            return MarkerPhase.TRIGGER;
        }
    }
}
