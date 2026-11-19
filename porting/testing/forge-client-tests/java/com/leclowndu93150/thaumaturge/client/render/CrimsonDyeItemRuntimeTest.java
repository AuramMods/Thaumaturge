package com.leclowndu93150.thaumaturge.client.render;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.equipment.ArmorLayerColors;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class CrimsonDyeItemRuntimeTest {
    private static final int LIGHT = 157;
    private static final int OVERLAY = 731;

    @Test
    void nativeRendererDrawsOnlyTheBaseQuadUntilCrimsonClothIsDyed() {
        var item = mock(ArmorItem.class);
        when(item.getMaterial()).thenReturn(TTMaterials.ARMOR_CULTIST_PLATE);
        var stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(item);
        var colors = new ItemColors() {
            @Override
            public int getColor(ItemStack input, int index) {
                return ArmorLayerColors.crimsonClothTint(input, index);
            }
        };
        var renderer = new ItemRenderer(null, null, mock(ModelManager.class), colors, null);
        var base = quad(0);
        var cloth = quad(ArmorLayerColors.CRIMSON_CLOTH_TINT_INDEX);
        var buffer = mock(VertexConsumer.class);

        renderer.renderQuadList(new PoseStack(), buffer, List.of(base, cloth), stack, LIGHT, OVERLAY);

        verifyQuad(buffer, base, 1.0F, 1.0F, 1.0F);
        verifyNoMoreInteractions(buffer);

        var display = new CompoundTag();
        display.putInt(DyeableLeatherItem.TAG_COLOR, 0x325A9F);
        when(stack.getTagElement(DyeableLeatherItem.TAG_DISPLAY)).thenReturn(display);
        buffer = mock(VertexConsumer.class);
        renderer.renderQuadList(new PoseStack(), buffer, List.of(base, cloth), stack, LIGHT, OVERLAY);
        verifyQuad(buffer, base, 1.0F, 1.0F, 1.0F);
        verifyQuad(buffer, cloth, 0x32 / 255.0F, 0x5A / 255.0F, 0x9F / 255.0F);
        verifyNoMoreInteractions(buffer);

        display.putInt(DyeableLeatherItem.TAG_COLOR, 0);
        buffer = mock(VertexConsumer.class);
        renderer.renderQuadList(new PoseStack(), buffer, List.of(cloth), stack, LIGHT, OVERLAY);
        verifyQuad(buffer, cloth, 0, 0, 0);
    }

    @Test
    void zeroRgbStillDrawsOrdinaryBlackQuads() {
        var colors = mock(ItemColors.class);
        var renderer = new ItemRenderer(null, null, mock(ModelManager.class), colors, null);
        var buffer = mock(VertexConsumer.class);
        var black = quad(ArmorLayerColors.CRIMSON_CLOTH_TINT_INDEX);
        renderer.renderQuadList(new PoseStack(), buffer, List.of(black), new ItemStack(Items.LEATHER_CHESTPLATE),
                LIGHT, OVERLAY);
        verifyQuad(buffer, black, 0, 0, 0);
    }

    private static BakedQuad quad(int tint) {
        return new BakedQuad(new int[32], tint, Direction.NORTH, mock(TextureAtlasSprite.class), false);
    }

    private static void verifyQuad(VertexConsumer buffer, BakedQuad quad, float red, float green, float blue) {
        verify(buffer).putBulkData(any(PoseStack.Pose.class), same(quad), eq(red), eq(green), eq(blue), eq(1.0F),
                eq(LIGHT), eq(OVERLAY), eq(true));
    }
}
