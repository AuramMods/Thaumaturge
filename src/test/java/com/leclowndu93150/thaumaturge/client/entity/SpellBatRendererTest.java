package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.FireBatModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.junit.jupiter.api.Test;

class SpellBatRendererTest {
    @Test
    void cullsBackFacesOfItsTwoSidedTranslucentMembranes() {
        var context = mock(EntityRendererProvider.Context.class);
        when(context.bakeLayer(TTModelLayers.FIRE_BAT)).thenReturn(FireBatModel.createLayer().bakeRoot());
        var renderer = new SpellBatRenderer(context);
        var texture = TTIds.rl("textures/entity/spellbat.png");

        assertSame(RenderType.entityTranslucentCull(texture), renderer.getModel().renderType(texture));
    }
}
