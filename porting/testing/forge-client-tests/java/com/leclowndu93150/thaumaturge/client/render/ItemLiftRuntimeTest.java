package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.render.blockentity.LegacyItemLift;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ItemLiftRuntimeTest {
    private final Minecraft client = mock(Minecraft.class);
    private final ItemRenderer renderer = mock(ItemRenderer.class, CALLS_REAL_METHODS);
    private final BlockEntityWithoutLevelRenderer custom = mock(BlockEntityWithoutLevelRenderer.class);
    private final IClientItemExtensions extensions = mock(IClientItemExtensions.class);
    private final BakedModel model = customModel();

    @AfterEach
    void clearMeasurements() {
        LegacyItemLift.clearBottomLiftCache();
    }

    @Test
    void measuresCompletedVerticesFromDifferentRenderTypesThroughNativeItemRenderer() {
        doAnswer(call -> {
            PoseStack poses = call.getArgument(2);
            MultiBufferSource buffers = call.getArgument(3);
            var solid = buffers.getBuffer(RenderType.solid());
            solid.defaultColor(255, 255, 255, 255);
            solid.vertex(poses.last().pose(), 0.0F, -0.375F, 0.0F);
            buffers.getBuffer(RenderType.cutout()).vertex(0.0F, 0.125F, 0.0F,
                    1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0, 0, 0.0F, 1.0F, 0.0F);
            solid.uv(0.0F, 0.0F).overlayCoords(0).uv2(0).normal(0.0F, 1.0F, 0.0F).endVertex();
            solid.unsetDefaultColor();
            solid.vertex(0.0, -5.0, 0.0);
            return null;
        }).when(custom).renderByItem(any(), any(), any(), any(), anyInt(), anyInt());

        withNativeRenderer(() -> assertEquals(0.625F,
                LegacyItemLift.bottomLift(new ItemStack(Items.STICK), ItemDisplayContext.GROUND)));
    }

    @Test
    void usesBakedBoundsIfCustomRendererEmitsAnInvalidVertex() {
        int[] vertices = new int[32];
        for (int i = 0; i < 4; i++) {
            vertices[i * 8 + 1] = Float.floatToRawIntBits(i % 2 == 0 ? 0.125F : 0.625F);
        }
        var quad = new BakedQuad(vertices, -1, Direction.UP, mock(TextureAtlasSprite.class), false);
        when(model.getQuads(isNull(), isNull(), any(), eq(ModelData.EMPTY), isNull())).thenReturn(List.of(quad));
        doAnswer(call -> {
            MultiBufferSource buffers = call.getArgument(3);
            buffers.getBuffer(RenderType.solid()).vertex(0.0, Double.NaN, 0.0).endVertex();
            return null;
        }).when(custom).renderByItem(any(), any(), any(), any(), anyInt(), anyInt());

        withNativeRenderer(() -> {
            var stack = new ItemStack(Items.STICK);
            assertEquals(0.125F, LegacyItemLift.bottomLift(stack, ItemDisplayContext.GROUND));
            assertEquals(0.225F, LegacyItemLift.centerLift(stack, ItemDisplayContext.GROUND), 1.0E-6F);
            assertEquals(0.0F, LegacyItemLift.bottomLift(ItemStack.EMPTY, ItemDisplayContext.GROUND));
        });
    }

    @Test
    void cachesModelIdentityContextAndNbtValuesWithAStableSnapshot() {
        var measurements = new AtomicInteger();
        doAnswer(call -> {
            measurements.incrementAndGet();
            ItemStack stack = call.getArgument(0);
            ItemDisplayContext context = call.getArgument(1);
            MultiBufferSource buffers = call.getArgument(3);
            float y = "Aa".equals(stack.getTag().getString("shape")) ? -0.25F : -0.5F;
            if (context == ItemDisplayContext.FIXED) {
                y -= 0.25F;
            }
            buffers.getBuffer(RenderType.solid()).vertex(0.0, y, 0.0).endVertex();
            return null;
        }).when(custom).renderByItem(any(), any(), any(), any(), anyInt(), anyInt());

        withNativeRenderer(() -> {
            var stack = new ItemStack(Items.STICK);
            var tag = stack.getOrCreateTag();
            tag.putString("shape", "Aa");
            int originalHash = tag.hashCode();
            assertEquals(0.25F, LegacyItemLift.bottomLift(stack, ItemDisplayContext.GROUND));
            assertEquals(0.25F, LegacyItemLift.bottomLift(stack.copyWithCount(12), ItemDisplayContext.GROUND));
            assertEquals(1, measurements.get());

            tag.putString("shape", "BB");
            assertEquals(originalHash, tag.hashCode());
            assertEquals(0.5F, LegacyItemLift.bottomLift(stack, ItemDisplayContext.GROUND));
            tag.putString("shape", "Aa");
            assertEquals(0.25F, LegacyItemLift.bottomLift(stack, ItemDisplayContext.GROUND));
            assertEquals(2, measurements.get());

            assertEquals(0.5F, LegacyItemLift.bottomLift(stack, ItemDisplayContext.FIXED));
            doReturn(customModel()).when(renderer).getModel(any(), isNull(), isNull(), eq(0));
            assertEquals(0.25F, LegacyItemLift.bottomLift(stack, ItemDisplayContext.GROUND));
            assertEquals(4, measurements.get());

            LegacyItemLift.clearBottomLiftCache();
            assertEquals(0.25F, LegacyItemLift.bottomLift(stack, ItemDisplayContext.GROUND));
            assertEquals(5, measurements.get());
        });
    }

    private void withNativeRenderer(Runnable test) {
        when(client.getItemRenderer()).thenReturn(renderer);
        doReturn(model).when(renderer).getModel(any(), isNull(), isNull(), eq(0));
        when(extensions.getCustomRenderer()).thenReturn(custom);
        try (var minecraft = mockStatic(Minecraft.class); var itemExtensions = mockStatic(IClientItemExtensions.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(client);
            itemExtensions.when(() -> IClientItemExtensions.of(any(ItemStack.class))).thenReturn(extensions);
            test.run();
        }
    }

    private static BakedModel customModel() {
        var model = mock(BakedModel.class);
        when(model.isCustomRenderer()).thenReturn(true);
        when(model.applyTransform(any(), any(), eq(false))).thenAnswer(call -> {
            PoseStack poses = call.getArgument(1);
            poses.translate(0.0F, 0.25F, 0.0F);
            return model;
        });
        return model;
    }
}
