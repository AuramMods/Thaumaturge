package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.data.sound.TTSoundDefinitionsProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlockSoundRuntimeTest {
    @Test
    void blockSoundTypesResolveRegisteredEventsForEveryAction() {
        check(TTSoundTypes.GORE, TTSounds.GORE.get(), 1.0F);
        check(TTSoundTypes.CRYSTAL, TTSounds.CRYSTAL.get(), 1.0F);
        check(TTSoundTypes.JAR, TTSounds.JAR.get(), 1.0F);
        check(TTSoundTypes.URN, TTSounds.URNBREAK.get(), 1.5F);
    }

    @Test
    void nativeSoundDatagenEmitsAllRegisteredEventsAgainstTheBundledAssets(@TempDir Path output) throws Exception {
        var resources = Path.of(System.getProperty("thaumaturge.testResourceRoot"));
        var helper = new ExistingFileHelper(List.of(resources), Set.of(), true, null, null);
        new TTSoundDefinitionsProvider(new PackOutput(output), helper).run(CachedOutput.NO_CACHE).join();
        var definition = JsonParser.parseString(Files.readString(output.resolve("assets/thaumaturge/sounds.json")))
                .getAsJsonObject();
        assertEquals(TTSounds.SOUNDS.getEntries().stream().map(entry -> entry.getId().getPath()).collect(Collectors.toSet()),
                definition.keySet());
        assertEquals(2, definition.getAsJsonObject("gore").getAsJsonArray("sounds").size());
        assertEquals(4, definition.getAsJsonObject("jar").getAsJsonArray("sounds").size());
        assertEquals(1, definition.getAsJsonObject("crystal").getAsJsonArray("sounds").size());
        assertEquals(1, definition.getAsJsonObject("urnbreak").getAsJsonArray("sounds").size());
    }

    private static void check(SoundType type, SoundEvent sound, float pitch) {
        assertEquals(0.5F, type.getVolume());
        assertEquals(pitch, type.getPitch());
        assertSame(sound, type.getBreakSound());
        assertSame(sound, type.getStepSound());
        assertSame(sound, type.getPlaceSound());
        assertSame(sound, type.getHitSound());
        assertSame(sound, type.getFallSound());
    }
}
