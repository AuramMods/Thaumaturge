package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.model.gear.AbstractTTArmorModel;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.junit.jupiter.api.Test;

class ArmorModelAnimationRuntimeTest {
    private static final LayerDefinition GEOMETRY =
            LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 32);
    private static final float PARENT_BODY_YAW = 0.41F;

    @Test
    void copiesTheWearerPoseThenAnimatesWithTheExactFrameBeforeDrawing() {
        var wearer = mock(LivingEntity.class);
        var model = render(wearer, EquipmentSlot.CHEST, TTMaterials.ARMOR_FORTRESS);

        assertEquals(List.of("setup", "draw"), model.calls);
        assertEquals(List.of(1.2F, 0.7F, 210.25F, -13.0F, 22.0F), model.animationInputs);
        assertEquals(PARENT_BODY_YAW, model.bodyYawAtSetup);
    }

    @Test
    void armorStandHelmetAndHatUseAllThreeStandHeadRotations() {
        var wearer = mock(ArmorStand.class);
        var zero = new Rotations(0, 0, 0);
        when(wearer.getHeadPose()).thenReturn(new Rotations(15, -30, 45));
        when(wearer.getBodyPose()).thenReturn(zero);
        when(wearer.getLeftArmPose()).thenReturn(zero);
        when(wearer.getRightArmPose()).thenReturn(zero);
        when(wearer.getLeftLegPose()).thenReturn(zero);
        when(wearer.getRightLegPose()).thenReturn(zero);
        var model = render(wearer, EquipmentSlot.HEAD, TTMaterials.ARMOR_FORTRESS);

        assertEquals(List.of("setup", "draw"), model.calls);
        assertEquals((float) Math.toRadians(15), model.head.xRot, 1.0E-6F);
        assertEquals((float) Math.toRadians(-30), model.head.yRot, 1.0E-6F);
        assertEquals((float) Math.toRadians(45), model.head.zRot, 1.0E-6F);
        assertEquals(model.head.xRot, model.hat.xRot);
        assertEquals(model.head.yRot, model.hat.yRot);
        assertEquals(model.head.zRot, model.hat.zRot);
    }

    @Test
    void harnessRetainsOnlyTheForgeCopiedPose() {
        var model = render(mock(LivingEntity.class), EquipmentSlot.CHEST, TTMaterials.ARMOR_THAUMOSTATIC_HARNESS);

        assertEquals(List.of("draw"), model.calls);
        assertEquals(PARENT_BODY_YAW, model.body.yRot);
    }

    @SuppressWarnings("unchecked")
    private static RecordingArmor render(LivingEntity wearer, EquipmentSlot slot, ArmorMaterial material) {
        var item = mock(ArmorItem.class);
        when(item.getMaterial()).thenReturn(material);
        when(item.getEquipmentSlot()).thenReturn(slot);
        var stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(item);
        when(wearer.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
        when(wearer.getItemBySlot(slot)).thenReturn(stack);
        var level = mock(Level.class);
        when(wearer.level()).thenReturn(level);
        when(level.registryAccess()).thenReturn(RegistryAccess.EMPTY);

        var wearerModel = new HumanoidModel<LivingEntity>(GEOMETRY.bakeRoot());
        wearerModel.body.yRot = PARENT_BODY_YAW;
        RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> parent = mock(RenderLayerParent.class);
        when(parent.getModel()).thenReturn(wearerModel);
        var layer = new HumanoidArmorLayer<>(parent, new HumanoidModel<>(GEOMETRY.bakeRoot()),
                new HumanoidModel<>(GEOMETRY.bakeRoot()), mock(ModelManager.class));
        var armor = new RecordingArmor();
        IClientItemExtensions extension = new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack equipped, EquipmentSlot slot,
                    HumanoidModel<?> original) {
                return armor;
            }
        };
        try (var extensions = mockStatic(IClientItemExtensions.class)) {
            extensions.when(() -> IClientItemExtensions.of(stack)).thenReturn(extension);
            var vertices = mock(VertexConsumer.class);
            layer.render(new PoseStack(), type -> vertices, 157, wearer, 1.2F, 0.7F, 0.25F, 210.25F, -13.0F, 22.0F);
        }
        return armor;
    }

    private static final class RecordingArmor extends AbstractTTArmorModel {
        private final List<String> calls = new ArrayList<>();
        private List<Float> animationInputs;
        private float bodyYawAtSetup;

        private RecordingArmor() {
            super(GEOMETRY.bakeRoot());
        }

        @Override
        public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                float netHeadYaw, float headPitch) {
            calls.add("setup");
            bodyYawAtSetup = body.yRot;
            animationInputs = List.of(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }

        @Override
        public void renderToBuffer(PoseStack poses, VertexConsumer buffer, int light, int overlay,
                float r, float g, float b, float a) {
            calls.add("draw");
        }
    }
}
