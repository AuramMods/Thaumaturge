package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity;

import com.leclowndu93150.thaumaturge.client.equipment.ArmorLayerColors;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererArmorDyeMixin {
    @WrapWithCondition(method = "renderQuadList", at = @At(value = "INVOKE", remap = false,
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFIIZ)V"))
    private boolean thaumaturge$hideUndyedCrimsonCloth(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
            float red, float green, float blue, float alpha, int light, int overlay, boolean readExistingColor,
            @Local(argsOnly = true) ItemStack stack, @Local(index = 11) int tint) {
        // Forge renders item tints as RGB. Zero marks a hidden layer on these specific models.
        if (tint != 0 || quad.getTintIndex() != ArmorLayerColors.CRIMSON_CLOTH_TINT_INDEX
                || !(stack.getItem() instanceof ArmorItem armor)) {
            return true;
        }
        var material = armor.getMaterial();
        return material != TTMaterials.ARMOR_CULTIST_PLATE
                && material != TTMaterials.ARMOR_CULTIST_ROBE
                && material != TTMaterials.ARMOR_CULTIST_RANGER
                && material != TTMaterials.ARMOR_CULTIST_CAPTAIN
                && material != TTMaterials.ARMOR_CULTIST_LEADER;
    }
}
