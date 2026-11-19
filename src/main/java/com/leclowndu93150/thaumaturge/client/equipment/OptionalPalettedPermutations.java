package com.leclowndu93150.thaumaturge.client.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/** Recolors trim bases supplied by optional backport packs. */
public record OptionalPalettedPermutations(List<ResourceLocation> textures, ResourceLocation paletteKey,
                                         Map<String, ResourceLocation> permutations) implements SpriteSource {
    private static final Codec<OptionalPalettedPermutations> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("textures").forGetter(OptionalPalettedPermutations::textures),
            ResourceLocation.CODEC.fieldOf("palette_key").forGetter(OptionalPalettedPermutations::paletteKey),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("permutations")
                    .forGetter(OptionalPalettedPermutations::permutations)
    ).apply(instance, OptionalPalettedPermutations::new));
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    @Override
    public void run(ResourceManager resources, Output output) {
        List<ResourceLocation> available = textures.stream()
                .filter(texture -> resources.getResource(TEXTURE_ID_CONVERTER.idToFile(texture)).isPresent())
                .toList();
        new PalettedPermutations(available, paletteKey, permutations).run(resources, output);
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
