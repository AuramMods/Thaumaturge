package com.leclowndu93150.thaumaturge.content.research.note;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.junit.jupiter.api.Test;

class ResearchNoteRegistryRuntimeTest {
    private static final ResourceKey<IAspect> COMPOUND = ResourceKey.create(IAspect.REGISTRY_KEY, TTIds.rl("port_note_compound"));

    private static Registry<IAspect> aspects() {
        var registry = new MappedRegistry<IAspect>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        for (var key : List.of(TTAspects.AER, TTAspects.IGNIS, TTAspects.AQUA)) {
            var primal = mock(IAspect.class);
            when(primal.isPrimal()).thenReturn(true);
            registry.register(key, primal, Lifecycle.stable());
        }
        var compound = mock(IAspect.class);
        var components = List.of(registry.getHolderOrThrow(TTAspects.AER), registry.getHolderOrThrow(TTAspects.IGNIS));
        when(compound.components()).thenReturn(List.copyOf(components));
        registry.register(COMPOUND, compound, Lifecycle.stable());
        return registry.freeze();
    }

    private static RegistryOps<net.minecraft.nbt.Tag> ops(Registry<IAspect> registry) {
        return RegistryOps.create(NbtOps.INSTANCE, new RegistryAccess.ImmutableRegistryAccess(List.of(registry)));
    }

    @Test
    void savedNotesResolveNewRegistryHoldersAndPruneOnlyCellsOutsideTheCompletedPath() {
        var original = aspects();
        var aer = original.getHolderOrThrow(TTAspects.AER);
        var ignis = original.getHolderOrThrow(TTAspects.IGNIS);
        var compound = original.getHolderOrThrow(COMPOUND);
        var start = new HexGrid.Hex(0, 0);
        var middle = start.neighbour(0);
        var finish = middle.neighbour(0);
        var unrelated = start.neighbour(2);
        var note = new ResearchNoteData(TTIds.rl("port_note"), 0, 0, false, 0, List.of())
                .withCell(start, ResearchNoteData.TYPE_ROOT, aer)
                .withCell(middle, ResearchNoteData.TYPE_PLACED, compound)
                .withCell(finish, ResearchNoteData.TYPE_ROOT, ignis)
                .withCell(unrelated, ResearchNoteData.TYPE_PLACED, original.getHolderOrThrow(TTAspects.AQUA));
        assertFalse(NoteRules.checkCompletion(note, holder -> !holder.is(COMPOUND)).complete());

        var saved = ResearchNoteData.CODEC.encodeStart(ops(original), note).getOrThrow(false, message -> fail(message));
        var reopened = aspects();
        var decoded = ResearchNoteData.CODEC.parse(ops(reopened), saved).getOrThrow(false, message -> fail(message));
        var reloadedAer = reopened.getHolderOrThrow(TTAspects.AER);
        var reloadedCompound = reopened.getHolderOrThrow(COMPOUND);
        assertNotSame(aer, reloadedAer);
        assertSame(reloadedAer, decoded.cellAt(start).aspectOrNull());
        assertTrue(NoteRules.connects(reloadedCompound, aer, holder -> true));
        assertTrue(NoteRules.connects(reloadedAer, reloadedCompound, holder -> true));

        var result = NoteRules.checkCompletion(decoded, holder -> true);
        assertTrue(result.complete());
        assertEquals(List.of(start, middle, finish), result.prunedCells().stream().map(ResearchNoteData.Cell::hex).toList());
    }
}
