package com.leclowndu93150.thaumaturge.client.render;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.client.equipment.OptionalPalettedPermutations;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class OptionalTrimAtlasRuntimeTest {
    @Test
    void atlasSchedulesPermutationsForEachAvailableBackportTexture() throws Exception {
        Path atlas = Path.of(System.getProperty("thaumaturge.testResourceRoot"),
                "assets/minecraft/atlases/armor_trims.json");
        var json = JsonParser.parseString(Files.readString(atlas)).getAsJsonObject();
        var sources = SpriteSources.FILE_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, message -> {
            throw new AssertionError(message);
        });
        var optional = sources.stream().filter(OptionalPalettedPermutations.class::isInstance)
                .findFirst().orElseThrow();
        var pack = mock(ResourceManager.class);
        var output = mock(SpriteSource.Output.class);

        optional.run(ResourceManager.Empty.INSTANCE, output);
        verifyNoInteractions(output);

        when(pack.getResource(new ResourceLocation("textures/trims/models/armor/bolt_leggings.png")))
                .thenReturn(Optional.of(mock(Resource.class)));
        optional.run(pack, output);

        for (String material : List.of("brass", "thaumium", "void_metal", "essentia_crystal")) {
            verify(output).add(eq(new ResourceLocation("trims/models/armor/bolt_leggings_thaumaturge_" + material)),
                    any(SpriteSource.SpriteSupplier.class));
        }
        verifyNoMoreInteractions(output);
    }
}
