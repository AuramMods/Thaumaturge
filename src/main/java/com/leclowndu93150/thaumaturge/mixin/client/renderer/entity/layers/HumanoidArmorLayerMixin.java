package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity.layers;

import com.leclowndu93150.thaumaturge.client.equipment.ArmorLayerColors;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimColors;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSpriteKey;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSpriteSource;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSprites;
import com.leclowndu93150.thaumaturge.client.model.gear.AbstractTTArmorModel;
import com.leclowndu93150.thaumaturge.compat.Colors.ARGB32;
import com.leclowndu93150.thaumaturge.content.equipment.ArmorMaterialData;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.client.ForgeHooksClient;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin implements ArmorTrimSpriteSource {
    @Shadow
    @Final
    private TextureAtlas armorTrimAtlas;

    @Unique
    private static final String RENDER_TRIM =
            "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V";

    @Unique
    private static final String RENDER_MODEL =
            "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V";

    @Unique
    private Function<ArmorTrimSpriteKey, Optional<TextureAtlasSprite>> thaumaturge$armorTrimSprites;

    @Unique
    private @Nullable AnimationFrame thaumaturge$animationFrame;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void thaumaturge$bindArmorTrimSprites(CallbackInfo ci) {
        TextureAtlas atlas = armorTrimAtlas;
        thaumaturge$armorTrimSprites = Util.memoize(key -> ArmorTrimSprites.find(atlas, key));
    }

    @WrapMethod(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    private void thaumaturge$withArmorAnimation(
            PoseStack poses, MultiBufferSource buffers, int light, LivingEntity entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
            float headPitch, Operation<Void> render) {
        AnimationFrame previous = thaumaturge$animationFrame;
        thaumaturge$animationFrame = new AnimationFrame(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        try {
            render.call(poses, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks,
                    netHeadYaw, headPitch);
        } finally {
            thaumaturge$animationFrame = previous;
        }
    }

    @ModifyExpressionValue(method = "renderArmorPiece", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;getArmorModelHook(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/Model;"))
    private Model thaumaturge$animateArmor(Model model, @Local(argsOnly = true) LivingEntity wearer,
            @Local(argsOnly = true) EquipmentSlot slot, @Local ArmorItem item) {
        AnimationFrame frame = thaumaturge$animationFrame;
        if (frame != null && model instanceof AbstractTTArmorModel armor
                && item.getMaterial() != TTMaterials.ARMOR_THAUMOSTATIC_HARNESS) {
            armor.setupAnim(wearer, frame.limbSwing(), frame.limbSwingAmount(), frame.ageInTicks(),
                    frame.netHeadYaw(), frame.headPitch());
            if (slot == EquipmentSlot.HEAD && wearer instanceof ArmorStand) {
                armor.hat.copyFrom(armor.head);
            }
        }
        return model;
    }

    @WrapOperation(method = "renderArmorPiece", at = {
            @At(value = "INVOKE", target = RENDER_MODEL, ordinal = 0, remap = false),
            @At(value = "INVOKE", target = RENDER_MODEL, ordinal = 2, remap = false)
    }, require = 2)
    private void thaumaturge$renderMaterialLayers(
            HumanoidArmorLayer<?, ?, ?> renderer, PoseStack poses, MultiBufferSource buffers, int packedLight,
            ArmorItem armor, Model model, boolean inner, float red, float green, float blue, ResourceLocation texture,
            Operation<Void> original, @Local(argsOnly = true) LivingEntity wearer,
            @Local(argsOnly = true) EquipmentSlot slot, @Local ItemStack stack) {
        if (!(armor.getMaterial() instanceof ArmorMaterialData material) || material.layers().size() <= 1) {
            original.call(renderer, poses, buffers, packedLight, armor, model, inner, red, green, blue, texture);
            return;
        }

        for (ArmorMaterialData.Layer layer : material.layers()) {
            int tint = ArmorLayerColors.wornTint(stack, material, layer);
            if (tint == 0) {
                continue;
            }
            var layerTexture = new ResourceLocation(ForgeHooksClient.getArmorTexture(
                    wearer, stack, layer.texture(inner).toString(), slot, layer.textureType()));
            original.call(renderer, poses, buffers, packedLight, armor, model, inner,
                    ARGB32.red(tint) / 255.0F, ARGB32.green(tint) / 255.0F, ARGB32.blue(tint) / 255.0F, layerTexture);
        }
    }

    @WrapWithCondition(method = "renderArmorPiece",
            at = @At(value = "INVOKE", target = RENDER_MODEL, ordinal = 1, remap = false))
    private boolean thaumaturge$useNativeArmorOverlay(
            HumanoidArmorLayer<?, ?, ?> renderer, PoseStack poses, MultiBufferSource buffers, int packedLight,
            ArmorItem armor, Model model, boolean inner, float red, float green, float blue, ResourceLocation texture) {
        return !(armor.getMaterial() instanceof ArmorMaterialData material) || material.layers().size() <= 1;
    }

    @ModifyExpressionValue(
            method = RENDER_TRIM,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite thaumaturge$armorTrimSprite(
            TextureAtlasSprite sprite,
            @Local(argsOnly = true) ArmorMaterial armorMaterial,
            @Local(argsOnly = true) ArmorTrim trim) {
        return thaumaturge$armorTrimSprite(new ArmorTrimSpriteKey(trim, armorMaterial), sprite);
    }

    @WrapOperation(
            method = RENDER_TRIM,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void thaumaturge$armorTrimTint(
            Model model,
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha,
            Operation<Void> original,
            @Local(argsOnly = true) ArmorTrim trim) {
        int color = ArmorTrimColors.of(trim.material());
        if (color == -1) {
            original.call(model, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        } else {
            original.call(model, poseStack, buffer, packedLight, packedOverlay,
                    ARGB32.red(color) / 255.0F, ARGB32.green(color) / 255.0F,
                    ARGB32.blue(color) / 255.0F, ARGB32.alpha(color) / 255.0F);
        }
    }

    @Override
    public TextureAtlasSprite thaumaturge$armorTrimSprite(ArmorTrimSpriteKey key, TextureAtlasSprite fallback) {
        return thaumaturge$armorTrimSprites.apply(key).orElse(fallback);
    }

    public record AnimationFrame(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                 float headPitch) {}
}
