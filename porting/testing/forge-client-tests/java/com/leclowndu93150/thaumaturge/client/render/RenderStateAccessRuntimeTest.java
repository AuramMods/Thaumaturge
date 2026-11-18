package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.VoidStreamRenderType;
import com.leclowndu93150.thaumaturge.client.warding.WardRenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.reflect.Modifier;
import java.util.List;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class RenderStateAccessRuntimeTest {
    @Test
    void theRequiredNativeFieldsArePublicAndRemainFinal() throws Exception {
        for (String name : List.of("ADDITIVE_TRANSPARENCY", "COLOR_DEPTH_WRITE", "COLOR_WRITE", "LEQUAL_DEPTH_TEST",
                "LIGHTMAP", "LIGHTNING_TRANSPARENCY", "NO_CULL", "NO_DEPTH_TEST", "NO_TRANSPARENCY",
                "RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER", "RENDERTYPE_LIGHTNING_SHADER",
                "TRANSLUCENT_TRANSPARENCY")) {
            var field = RenderStateShard.class.getField(name);
            assertTrue(Modifier.isFinal(field.getModifiers()), name);
            assertNotNull(field.get(null), name);
        }
    }

    @Test
    void actualRenderTypesUseTheNativeStatesAndTheirAuthoredVertexFormats() throws Exception {
        var texture = new ResourceLocation("thaumaturge", "textures/effect/eldritch_orb.png");
        var additive = TTRenderTypes.fxAdditive(texture);
        var noDepth = TTRenderTypes.additiveTexturedNoDepth(texture);
        assertSame(additive, TTRenderTypes.fxAdditive(texture));
        assertSame(DefaultVertexFormat.PARTICLE, additive.format());
        assertSame(DefaultVertexFormat.POSITION_TEX_COLOR, noDepth.format());
        assertNativeStates(additive, RenderStateShard.ADDITIVE_TRANSPARENCY, RenderStateShard.LEQUAL_DEPTH_TEST,
                RenderStateShard.COLOR_WRITE, RenderStateShard.NO_CULL, RenderStateShard.LIGHTMAP);
        assertNativeStates(noDepth, RenderStateShard.ADDITIVE_TRANSPARENCY, RenderStateShard.NO_DEPTH_TEST,
                RenderStateShard.COLOR_WRITE, RenderStateShard.NO_CULL);
        assertSame(DefaultVertexFormat.POSITION_TEX_COLOR, WardRenderType.ADDITIVE.format());
        assertNativeStates(WardRenderType.ADDITIVE, RenderStateShard.ADDITIVE_TRANSPARENCY,
                RenderStateShard.LEQUAL_DEPTH_TEST, RenderStateShard.COLOR_WRITE, RenderStateShard.NO_CULL);
        assertNativeStates(VoidStreamRenderType.TRANSLUCENT, RenderStateShard.TRANSLUCENT_TRANSPARENCY,
                RenderStateShard.LEQUAL_DEPTH_TEST, RenderStateShard.COLOR_DEPTH_WRITE, RenderStateShard.NO_CULL);
    }

    private static void assertNativeStates(RenderType layer, RenderStateShard... expected) throws Exception {
        assertEquals(VertexFormat.Mode.QUADS, layer.mode());
        var state = layer.getClass().getDeclaredField("state");
        state.setAccessible(true);
        var states = RenderType.CompositeState.class.getDeclaredField("states");
        states.setAccessible(true);
        var applied = (List<?>) states.get(state.get(layer));
        for (var shard : expected) {
            assertTrue(applied.contains(shard), shard.toString());
        }
    }
}
