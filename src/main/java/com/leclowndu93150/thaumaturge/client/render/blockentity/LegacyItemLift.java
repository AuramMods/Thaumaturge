package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class LegacyItemLift {
    public static final float LEGACY_CENTER_Y = 0.35F;
    private static final long QUAD_SEED = 42L;
    private static final int MAX_BOTTOM_LIFT_CACHE_ENTRIES = 512;
    private static final float MAX_REASONABLE_RENDER_BOUND = 64.0F;
    private static final Map<BottomLiftKey, Float> BOTTOM_LIFT_CACHE = new LinkedHashMap<>();

    private LegacyItemLift() {}

    public static float centerLift(ItemStack stack, ItemDisplayContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        float[] bounds = bakedYBounds(model, context);
        if (!usableBounds(bounds)) {
            return LEGACY_CENTER_Y;
        }
        return LEGACY_CENTER_Y - (bounds[0] + bounds[1]) * 0.5F;
    }

    /**
     * Returns the lift needed to put the lowest rendered vertex at Y=0.
     *
     * <p>The first lookup renders the item into a bounds-only vertex consumer. This follows Minecraft's actual item
     * rendering path, so special/BEWLR renderers and third-party custom item renderers can be measured too. The result
     * is cached by resolved model identity, item and stack NBT, and display context, so subsequent frames are a cheap
     * cache lookup.
     *
     * <p>If a custom renderer fails or emits unusable bounds, this falls back to the baked-model quad calculation. If
     * neither path can provide valid geometry, zero lift is used as a safe final fallback.
     */
    public static float bottomLift(ItemStack stack, ItemDisplayContext context) {
        if (stack.isEmpty()) {
            return 0.0F;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        CompoundTag tag = stack.getTag();
        var lookup = new BottomLiftKey(model, stack.getItem(), context, tag);
        Float cached = BOTTOM_LIFT_CACHE.get(lookup);
        if (cached != null) {
            return cached;
        }

        var storedKey = new BottomLiftKey(model, lookup.item(), context, tag == null ? null : tag.copy());
        float lift = renderedBottomLift(minecraft, stack, context);
        if (!Float.isFinite(lift)) {
            float[] bounds = bakedYBounds(model, context);
            lift = usableBounds(bounds) ? -bounds[0] : 0.0F;
        }

        cacheBottomLift(storedKey, lift);
        return lift;
    }

    /** Clears cached measurements after models are rebuilt during a resource reload. */
    public static void clearBottomLiftCache() {
        BOTTOM_LIFT_CACHE.clear();
    }

    private static float renderedBottomLift(Minecraft minecraft, ItemStack stack, ItemDisplayContext context) {
        RenderBounds bounds = new RenderBounds();
        Map<RenderType, VertexConsumer> consumers = new IdentityHashMap<>();
        MultiBufferSource measuringBuffers =
                renderType -> consumers.computeIfAbsent(renderType, ignored -> new BoundsVertexConsumer(bounds));
        PoseStack poseStack = new PoseStack();

        try {
            minecraft
                    .getItemRenderer()
                    .renderStatic(
                            stack,
                            context,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            measuringBuffers,
                            minecraft.level,
                            0);
        } catch (RuntimeException | LinkageError ignored) {
            return Float.NaN;
        }

        return bounds.usable() ? -bounds.minY : Float.NaN;
    }

    private static float[] bakedYBounds(BakedModel model, ItemDisplayContext context) {
        RandomSource random = RandomSource.create(QUAD_SEED);
        PoseStack poseStack = new PoseStack();
        model = model.applyTransform(context, poseStack, false);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        Matrix4f matrix = poseStack.last().pose();

        float[] bounds = {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        accumulateY(model.getQuads(null, null, random, ModelData.EMPTY, null), matrix, bounds);
        for (Direction direction : Direction.values()) {
            accumulateY(model.getQuads(null, direction, random, ModelData.EMPTY, null), matrix, bounds);
        }
        return bounds;
    }

    private static boolean usableBounds(float[] bounds) {
        return Float.isFinite(bounds[0])
                && Float.isFinite(bounds[1])
                && bounds[0] <= bounds[1]
                && Math.abs(bounds[0]) <= MAX_REASONABLE_RENDER_BOUND
                && Math.abs(bounds[1]) <= MAX_REASONABLE_RENDER_BOUND;
    }

    private static void cacheBottomLift(BottomLiftKey key, float lift) {
        if (BOTTOM_LIFT_CACHE.size() >= MAX_BOTTOM_LIFT_CACHE_ENTRIES) {
            var iterator = BOTTOM_LIFT_CACHE.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
        BOTTOM_LIFT_CACHE.put(key, lift);
    }

    private record BottomLiftKey(BakedModel model, Item item, ItemDisplayContext context, @Nullable CompoundTag tag) {
        @Override
        public boolean equals(Object other) {
            return other instanceof BottomLiftKey key
                    && model == key.model
                    && item == key.item
                    && context == key.context
                    && Objects.equals(tag, key.tag);
        }

        @Override
        public int hashCode() {
            int hash = System.identityHashCode(model);
            hash = 31 * hash + System.identityHashCode(item);
            hash = 31 * hash + context.ordinal();
            return 31 * hash + Objects.hashCode(tag);
        }
    }

    private static void accumulateY(List<BakedQuad> quads, Matrix4f matrix, float[] bounds) {
        Vector3f corner = new Vector3f();
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int base = vertex * stride;
                corner.set(
                        Float.intBitsToFloat(vertices[base]),
                        Float.intBitsToFloat(vertices[base + 1]),
                        Float.intBitsToFloat(vertices[base + 2]));
                matrix.transformPosition(corner);
                bounds[0] = Math.min(bounds[0], corner.y);
                bounds[1] = Math.max(bounds[1], corner.y);
            }
        }
    }

    private static final class RenderBounds {
        private float minY = Float.POSITIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private boolean invalid;

        private void include(float y) {
            if (!Float.isFinite(y)) {
                invalid = true;
                return;
            }
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        private boolean usable() {
            return !invalid
                    && Float.isFinite(minY)
                    && Float.isFinite(maxY)
                    && minY <= maxY
                    && Math.abs(minY) <= MAX_REASONABLE_RENDER_BOUND
                    && Math.abs(maxY) <= MAX_REASONABLE_RENDER_BOUND;
        }
    }

    private static final class BoundsVertexConsumer implements VertexConsumer {
        private final RenderBounds bounds;
        private float y = Float.NaN;

        private BoundsVertexConsumer(RenderBounds bounds) {
            this.bounds = bounds;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.y = (float) y;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float normalX, float normalY, float normalZ) {
            return this;
        }

        @Override
        public void endVertex() {
            bounds.include(y);
            y = Float.NaN;
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {}

        @Override
        public void unsetDefaultColor() {}
    }
}
