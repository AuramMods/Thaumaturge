package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import java.nio.file.Path;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.junit.jupiter.api.Test;

class DynamicTreesPackRuntimeTest {
    @Test
    void forgeSelectsModelsAndLootOnlyWithDynamicTreesInstalled() throws Exception {
        ModList mods = mock(ModList.class);
        IModFileInfo thaumaturge = mock(IModFileInfo.class);
        IModFile file = mock(IModFile.class);
        Path path = Path.of(System.getProperty("thaumaturge.testResourceRoot"),
                TTIds.DYNAMIC_TREES_RESOURCE_PACK.getPath());
        when(mods.getModFileById(TTIds.MODID)).thenReturn(thaumaturge);
        when(thaumaturge.getFile()).thenReturn(file);
        when(file.findResource(TTIds.DYNAMIC_TREES_RESOURCE_PACK.getPath())).thenReturn(path);

        try (var loader = mockStatic(ModList.class)) {
            loader.when(ModList::get).thenReturn(mods);
            assertTrue(repository(PackType.CLIENT_RESOURCES).getSelectedPacks().isEmpty());
            assertTrue(repository(PackType.SERVER_DATA).getSelectedPacks().isEmpty());

            when(mods.isLoaded(TTIds.DYNAMIC_TREES)).thenReturn(true);
            assertSelectedResource(PackType.CLIENT_RESOURCES, TTIds.rl("textures/block/log_greatwood_top_thick.png"));
            assertSelectedResource(PackType.SERVER_DATA, TTIds.rl("loot_tables/trees/branches/greatwood.json"));
        }
    }

    private static PackRepository repository(PackType type) {
        PackRepository repository = new PackRepository();
        DynamicTreesResourcePack.onAddPackFinders(new AddPackFindersEvent(type, repository::addPackFinder));
        repository.reload();
        return repository;
    }

    private static void assertSelectedResource(PackType type, ResourceLocation resource) throws Exception {
        var repository = repository(type);
        String id = "mod/" + TTIds.DYNAMIC_TREES_RESOURCE_PACK;
        assertEquals(Set.of(id), Set.copyOf(repository.getSelectedIds()));
        var pack = repository.getPack(id);
        assertTrue(pack.isRequired());
        assertEquals(PackCompatibility.COMPATIBLE, pack.getCompatibility());
        try (var opened = pack.open(); var stream = opened.getResource(type, resource).get()) {
            assertTrue(stream.read() >= 0);
        }
    }
}
