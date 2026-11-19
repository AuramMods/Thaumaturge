package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ThaumicSlimeRenderer extends MobRenderer<ThaumicSlime, SlimeModel<ThaumicSlime>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/entity/thaumic_slime.png");
    private static final float SHADOW_PER_SIZE = 0.25F;

    public ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), SHADOW_PER_SIZE);
        this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    public void render(ThaumicSlime entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight) {
        this.shadowRadius = entity.getSize() * SHADOW_PER_SIZE;
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    protected void scale(ThaumicSlime entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish) / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);
        poseStack.scale(stretch * size, 1.0F / stretch * size, stretch * size);
    }

    @Override
    public ResourceLocation getTextureLocation(ThaumicSlime entity) {
        return TEXTURE;
    }
}
