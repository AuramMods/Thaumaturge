package com.leclowndu93150.thaumaturge.data.labyrinth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateNbtWriterRuntimeTest {
    @Test
    void generatedTemplateLoadsThroughTheNativeStructureManager(@TempDir Path root) throws Exception {
        Path pack = root.resolve("pack");
        var id = new ResourceLocation("thaumaturge", "outer_lands/fixture");
        var source = new MarkerRoom();
        TemplateNbtWriter.save(CachedOutput.NO_CACHE, TemplateNbtWriter.structures(new PackOutput(pack)), id,
                TemplateNbtWriter.write(source)).join();
        assertTrue(Files.isRegularFile(pack.resolve("data/thaumaturge/structures/outer_lands/fixture.nbt")));

        try (var storage = LevelStorageSource.createDefault(root.resolve("worlds")).createAccess("fixture");
             var packs = new MultiPackResourceManager(PackType.SERVER_DATA,
                     List.of(new PathPackResources("generated", pack, false)))) {
            var manager = new StructureTemplateManager(
                    packs, storage, DataFixers.getDataFixer(), BuiltInRegistries.BLOCK.asLookup());
            assertTrue(manager.listTemplates().anyMatch(id::equals));
            var template = manager.get(id).orElseThrow();
            assertEquals(new Vec3i(2, 1, 1), template.getSize());
            var markers = template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.STRUCTURE_BLOCK);
            assertEquals(1, markers.size());
            assertEquals(new BlockPos(1, 0, 0), markers.get(0).pos());
            assertEquals("DATA", markers.get(0).nbt().getString("mode"));
            assertEquals("{\"fixture\":true}", markers.get(0).nbt().getString("metadata"));
        }
    }

    private static final class MarkerRoom implements TemplateSource {
        @Override public int sizeX() { return 2; }
        @Override public int sizeY() { return 1; }
        @Override public int sizeZ() { return 1; }

        @Override
        public BlockState get(int x, int y, int z) {
            return x == 0 ? Blocks.STONE.defaultBlockState() : Blocks.STRUCTURE_BLOCK.defaultBlockState();
        }

        @Override
        public Optional<String> metadata(int x, int y, int z) {
            return x == 1 ? Optional.of("{\"fixture\":true}") : Optional.empty();
        }
    }
}
