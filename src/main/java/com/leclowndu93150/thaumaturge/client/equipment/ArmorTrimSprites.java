package com.leclowndu93150.thaumaturge.client.equipment;

import java.util.Optional;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.TrimMaterial;

public final class ArmorTrimSprites {
    private static final String TRIM_TEXTURE_ROOT = "trims/entity/";

    private ArmorTrimSprites() {}

    public static Optional<TextureAtlasSprite> find(TextureAtlas armorTrimAtlas, ArmorTrimSpriteKey key) {
        ResourceLocation armorMaterial = ResourceLocation.tryParse(key.armorMaterial().getName());
        if (armorMaterial == null) {
            return Optional.empty();
        }
        TrimMaterial material = key.trim().material().value();
        String suffix = material.overrideArmorMaterials().getOrDefault(key.armorMaterial(), material.assetName());
        String root = TRIM_TEXTURE_ROOT + armorMaterial.getPath() + "/";
        ResourceLocation spriteId = key.trim().pattern().value().assetId().withPath(path -> root + path + "_" + suffix);
        TextureAtlasSprite sprite = armorTrimAtlas.getSprite(spriteId);
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())
                ? Optional.empty()
                : Optional.of(sprite);
    }
}
