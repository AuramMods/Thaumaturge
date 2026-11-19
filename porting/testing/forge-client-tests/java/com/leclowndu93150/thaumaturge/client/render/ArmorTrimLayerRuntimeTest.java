package com.leclowndu93150.thaumaturge.client.render;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import org.junit.jupiter.api.Test;

class ArmorTrimLayerRuntimeTest {
    @Test
    void nativeTrimRenderUsesTheCustomArmorSpriteAndEssentiaTint() throws Exception {
        var armor = mock(ArmorMaterial.class);
        when(armor.getName()).thenReturn("thaumaturge:cultist_plate");
        var trim = trim(TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET, 0x2364B1, Map.of());
        var customId = id("trims/entity/cultist_plate/arcane_thaumaturge_essentia_crystal");
        var atlas = mock(TextureAtlas.class);
        var fallback = sprite(trim.outerTexture(armor));
        var custom = sprite(customId);
        when(atlas.getSprite(trim.outerTexture(armor))).thenReturn(fallback);
        when(atlas.getSprite(customId)).thenReturn(custom);
        var consumer = mock(VertexConsumer.class);
        when(custom.wrap(any(VertexConsumer.class))).thenReturn(consumer);

        var model = renderTrim(atlas, armor, trim, false);

        verify(model).renderToBuffer(any(PoseStack.class), eq(consumer), eq(157), eq(OverlayTexture.NO_OVERLAY),
                eq(0x23 / 255.0F), eq(0x64 / 255.0F), eq(0xB1 / 255.0F), eq(1.0F));
    }

    @Test
    void missingCustomSpriteUsesTheNativeLeggingsSpriteAndMaterialOverride() throws Exception {
        var trim = trim("iron", 0, Map.of(ArmorMaterials.IRON, "iron_darker"));
        var atlas = mock(TextureAtlas.class);
        var customId = id("trims/entity/iron/arcane_iron_darker");
        var missing = sprite(MissingTextureAtlasSprite.getLocation());
        var fallback = sprite(trim.innerTexture(ArmorMaterials.IRON));
        when(atlas.getSprite(customId)).thenReturn(missing);
        when(atlas.getSprite(trim.innerTexture(ArmorMaterials.IRON))).thenReturn(fallback);
        var consumer = mock(VertexConsumer.class);
        when(fallback.wrap(any(VertexConsumer.class))).thenReturn(consumer);

        var model = renderTrim(atlas, ArmorMaterials.IRON, trim, true);

        verify(atlas).getSprite(customId);
        verify(model).renderToBuffer(any(PoseStack.class), eq(consumer), eq(157), eq(OverlayTexture.NO_OVERLAY),
                eq(1.0F), eq(1.0F), eq(1.0F), eq(1.0F));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Model renderTrim(TextureAtlas atlas, ArmorMaterial armor, ArmorTrim trim, boolean inner)
            throws Exception {
        var models = mock(ModelManager.class);
        when(models.getAtlas(Sheets.ARMOR_TRIMS_SHEET)).thenReturn(atlas);
        var layer = new HumanoidArmorLayer(mock(RenderLayerParent.class), null, null, models);
        var model = mock(Model.class);
        var buffers = mock(MultiBufferSource.class);
        when(buffers.getBuffer(Sheets.armorTrimsSheet())).thenReturn(mock(VertexConsumer.class));
        var method = HumanoidArmorLayer.class.getDeclaredMethod("renderTrim", ArmorMaterial.class, PoseStack.class,
                MultiBufferSource.class, int.class, ArmorTrim.class, Model.class, boolean.class);
        method.setAccessible(true);
        method.invoke(layer, armor, new PoseStack(), buffers, 157, trim, model, inner);
        return model;
    }

    private static ArmorTrim trim(String asset, int color, Map<ArmorMaterials, String> overrides) {
        var pattern = new TrimPattern(id("arcane"), Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE.builtInRegistryHolder(),
                Component.literal("Arcane"));
        var material = TrimMaterial.create(asset, Items.IRON_INGOT, 0.2F,
                Component.literal("Fixture").withStyle(style -> style.withColor(color)), overrides);
        return new ArmorTrim(Holder.direct(material), Holder.direct(pattern));
    }

    private static TextureAtlasSprite sprite(ResourceLocation name) {
        var contents = mock(SpriteContents.class);
        when(contents.name()).thenReturn(name);
        var sprite = mock(TextureAtlasSprite.class);
        when(sprite.contents()).thenReturn(contents);
        return sprite;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("thaumaturge", path);
    }
}
