package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class ArmorMaterialLayersRuntimeTest {
    private static final int WHITE = 0xFFFFFF;
    private static final int ROBE_PURPLE = 0x6A3880;
    private static final int LIGHT = 157;

    @Test
    void outerClothAndInnerVoidRobesKeepTheirAuthoredOrderAndTints() {
        var cloth = mock(ArmorItem.class);
        when(cloth.getMaterial()).thenReturn(TTMaterials.ARMOR_ROBES);
        when(cloth.getEquipmentSlot()).thenReturn(EquipmentSlot.CHEST);
        assertEquals(List.of(pass("robes_layer_1", ROBE_PURPLE), pass("robes_layer_1_over", WHITE)),
                render(equipped(cloth), EquipmentSlot.CHEST));

        var voidRobe = mock(ArmorItem.class, withSettings().extraInterfaces(DyeableLeatherItem.class));
        when(voidRobe.getMaterial()).thenReturn(TTMaterials.ARMOR_VOID_ROBE);
        when(voidRobe.getEquipmentSlot()).thenReturn(EquipmentSlot.LEGS);
        var stack = equipped(voidRobe);
        setDye(stack, 0x1E642D);
        assertEquals(List.of(pass("void_robe_layer_2_over", ROBE_PURPLE), pass("void_robe_layer_2", WHITE)),
                render(stack, EquipmentSlot.LEGS));
    }

    @Test
    void crimsonClothDrawsOnlyWithDyeAndRetainsTheNativeTextureHookAndGlint() {
        var armor = mock(ArmorItem.class, withSettings().extraInterfaces(DyeableLeatherItem.class));
        when(armor.getMaterial()).thenReturn(TTMaterials.ARMOR_CULTIST_PLATE);
        when(armor.getEquipmentSlot()).thenReturn(EquipmentSlot.LEGS);
        var stack = equipped(armor);
        when(armor.getArmorTexture(any(), any(), any(), any())).thenReturn(null);
        assertEquals(List.of(pass("cultist_plate_layer_2", WHITE)), render(stack, EquipmentSlot.LEGS));

        setDye(stack, 0x325A9F);
        when(stack.hasFoil()).thenReturn(true);
        var zombieTexture = new ResourceLocation("thaumaturge", "textures/models/armor/zombie_plate_layer_2.png");
        when(armor.getArmorTexture(any(), any(), any(), any())).thenReturn(zombieTexture.toString());
        assertEquals(List.of(pass("zombie_plate_layer_2", WHITE), pass("zombie_plate_layer_2", 0x325A9F),
                        new Draw(RenderType.armorEntityGlint(), WHITE)),
                render(stack, EquipmentSlot.LEGS));
    }

    @Test
    void vanillaLeatherKeepsItsNativeDyeAndOverlayPasses() {
        var stack = new ItemStack(Items.LEATHER_LEGGINGS);
        ((DyeableLeatherItem) stack.getItem()).setColor(stack, 0x13579B);
        assertEquals(List.of(
                        new Draw(RenderType.armorCutoutNoCull(location("minecraft", "leather_layer_2")), 0x13579B),
                        new Draw(RenderType.armorCutoutNoCull(location("minecraft", "leather_layer_2_overlay")), WHITE)),
                render(stack, EquipmentSlot.LEGS));
    }

    private static ItemStack equipped(ArmorItem armor) {
        var stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(armor);
        return stack;
    }

    private static void setDye(ItemStack stack, int color) {
        var display = new CompoundTag();
        display.putInt(DyeableLeatherItem.TAG_COLOR, color);
        when(stack.getTagElement(DyeableLeatherItem.TAG_DISPLAY)).thenReturn(display);
    }

    private static Draw pass(String texture, int rgb) {
        return new Draw(RenderType.armorCutoutNoCull(location("thaumaturge", texture)), rgb);
    }

    private static ResourceLocation location(String namespace, String texture) {
        return new ResourceLocation(namespace, "textures/models/armor/" + texture + ".png");
    }

    @SuppressWarnings("unchecked")
    private static List<Draw> render(ItemStack stack, EquipmentSlot slot) {
        var layerDefinition = LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 32);
        var humanoid = new HumanoidModel<LivingEntity>(layerDefinition.bakeRoot());
        RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> parent = mock(RenderLayerParent.class);
        when(parent.getModel()).thenReturn(humanoid);
        var colorModel = mock(Model.class);
        var layer = new HumanoidArmorLayer<>(parent, humanoid, humanoid, mock(ModelManager.class)) {
            @Override
            protected Model getArmorModelHook(LivingEntity wearer, ItemStack item, EquipmentSlot equipmentSlot,
                    HumanoidModel<LivingEntity> original) {
                return colorModel;
            }
        };

        var draws = new ArrayList<Draw>();
        Map<VertexConsumer, RenderType> bufferTypes = new IdentityHashMap<>();
        MultiBufferSource buffers = type -> {
            var consumer = mock(VertexConsumer.class);
            bufferTypes.put(consumer, type);
            return consumer;
        };
        doAnswer(call -> {
            assertEquals(LIGHT, call.getArgument(2, Integer.class).intValue());
            assertEquals(OverlayTexture.NO_OVERLAY, call.getArgument(3, Integer.class).intValue());
            assertEquals(1.0F, call.getArgument(7, Float.class).floatValue());
            int color = Math.round(call.getArgument(4, Float.class) * 255.0F) << 16
                    | Math.round(call.getArgument(5, Float.class) * 255.0F) << 8
                    | Math.round(call.getArgument(6, Float.class) * 255.0F);
            draws.add(new Draw(bufferTypes.get(call.getArgument(1)), color));
            return null;
        }).when(colorModel).renderToBuffer(any(), any(), anyInt(), anyInt(), anyFloat(), anyFloat(), anyFloat(), anyFloat());

        var wearer = mock(LivingEntity.class);
        var level = mock(Level.class);
        when(wearer.level()).thenReturn(level);
        when(level.registryAccess()).thenReturn(RegistryAccess.EMPTY);
        when(wearer.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
        when(wearer.getItemBySlot(slot)).thenReturn(stack);
        layer.render(new PoseStack(), buffers, LIGHT, wearer, 0, 0, 0, 0, 0, 0);
        return draws;
    }

    private record Draw(RenderType type, int rgb) {}
}
