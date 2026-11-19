package com.leclowndu93150.thaumaturge.client.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorLayerColors;
import com.leclowndu93150.thaumaturge.content.equipment.ArmorMaterialData;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class RobeArmorLayerParityTest {
    @Test
    void standardRobeKeepsTheTintedBaseAndPlainOverlay() {
        List<ArmorMaterialData.Layer> layers = TTMaterials.ARMOR_ROBES.layers();

        assertEquals(2, layers.size());
        assertEquals(
                TTIds.rl("textures/models/armor/robes_layer_1.png"),
                layers.get(0).texture(false));
        assertTrue(layers.get(0).dyeable());
        assertEquals(
                TTIds.rl("textures/models/armor/robes_layer_1_over.png"),
                layers.get(1).texture(false));
        assertFalse(layers.get(1).dyeable());
    }

    @Test
    void voidRobeKeepsTheTintedOverlayAndPlainBaseInSourceOrder() {
        List<ArmorMaterialData.Layer> layers = TTMaterials.ARMOR_VOID_ROBE.layers();

        assertEquals(2, layers.size());
        assertEquals(
                TTIds.rl("textures/models/armor/void_robe_layer_1_over.png"),
                layers.get(0).texture(false));
        assertTrue(layers.get(0).dyeable());
        assertEquals(
                TTIds.rl("textures/models/armor/void_robe_layer_2_over.png"),
                layers.get(0).texture(true));
        assertEquals(
                TTIds.rl("textures/models/armor/void_robe_layer_1.png"),
                layers.get(1).texture(false));
        assertFalse(layers.get(1).dyeable());
    }

    @Test
    void registeredRobePiecesUseTheSourceUndyedFallbackColor() {
        for (Item item : List.of(TTItems.CLOTH_CHEST.get(), TTItems.CLOTH_LEGS.get(), TTItems.CLOTH_BOOTS.get(),
                TTItems.VOID_ROBE_HELM.get(), TTItems.VOID_ROBE_CHEST.get(), TTItems.VOID_ROBE_LEGS.get())) {
            ArmorMaterialData material = (ArmorMaterialData) ((ArmorItem) item).getMaterial();
            assertEquals(0xFF6A3880, ArmorLayerColors.wornTint(new ItemStack(item), material, material.layers().get(0)),
                    item::toString);
        }
    }
}
