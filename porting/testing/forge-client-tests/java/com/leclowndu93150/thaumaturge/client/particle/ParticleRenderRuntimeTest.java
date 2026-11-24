package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL11C;

class ParticleRenderRuntimeTest {
    @Test
    void particleSheetDoesNotEndAnAsyncParticlesFormatProbe() {
        var layer = TTParticleLayers.translucent(TTParticleSheets.sheet("smoke_spiral"));
        var probe = mock(Tesselator.class);
        var builder = mock(BufferBuilder.class);
        when(probe.getBuilder()).thenReturn(builder);
        when(builder.endOrDiscardIfEmpty()).thenThrow(new UnsupportedOperationException("probe has no vertices"));
        try (var render = mockStatic(RenderSystem.class); var upload = mockStatic(BufferUploader.class)) {
            layer.begin(builder, mock(TextureManager.class));
            layer.end(probe);
            verify(builder).begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            render.verifyNoInteractions();
            upload.verifyNoInteractions();
        }
    }

    @Test
    void shaderPackSheetsUseTranslucentProgramAndScopeTheirBlendMode() {
        var sparkle = TTParticleLayers.additive(TTParticleSheets.sheet("sparkle"));
        var smoke = TTParticleLayers.translucent(TTParticleSheets.sheet("crimson_smoke"));
        var shader = mock(ShaderInstance.class);
        var tesselator = mock(Tesselator.class);
        var builder = mock(BufferBuilder.class);
        var geometry = mock(BufferBuilder.RenderedBuffer.class);
        var state = new ArrayList<String>();
        when(tesselator.getBuilder()).thenReturn(builder);
        when(builder.building()).thenReturn(true);
        when(builder.endOrDiscardIfEmpty()).thenReturn(geometry);
        var failure = new IllegalStateException("fixture draw failure");
        try (var iris = mockStatic(IrisCompat.class); var render = mockStatic(RenderSystem.class);
                var gl = mockStatic(GlStateManager.class); var upload = mockStatic(BufferUploader.class)) {
            iris.when(IrisCompat::overridesShaders).thenReturn(true);
            iris.when(IrisCompat::particleTranslucentShader).thenReturn(shader);
            render.when(() -> RenderSystem.setShader(any())).thenAnswer(call -> {
                Supplier<ShaderInstance> supplier = call.getArgument(0);
                assertSame(shader, supplier.get());
                return null;
            });
            upload.when(() -> BufferUploader.drawWithShader(geometry)).thenAnswer(call -> {
                assertTrue(ParticleSheet.isShaderPackDraw());
                state.add(ParticleSheet.isAdditiveShaderPackDraw() ? "additive" : "translucent");
                throw failure;
            });

            assertSame(failure, assertThrows(IllegalStateException.class, () -> sparkle.end(tesselator)));
            assertFalse(ParticleSheet.isShaderPackDraw());
            assertSame(failure, assertThrows(IllegalStateException.class, () -> smoke.end(tesselator)));
            assertFalse(ParticleSheet.isShaderPackDraw());
            assertEquals(List.of("additive", "translucent"), state);
            render.verify(() -> RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
                    GlStateManager.DestFactor.ONE));
        }
    }

    @Test
    void nativeRenderOrdersNitorLayers() throws Exception {
        var core = TTParticleLayers.additive(TTParticleSheets.sheet("nitor_core"));
        var flame = TTParticleLayers.additiveSoft(TTParticleSheets.sheet("wisp_flame"));
        var other = TTParticleLayers.translucentNoDepth(TTParticleSheets.sheet("sparkle"));
        var draws = new ArrayList<String>();
        var textures = mock(TextureManager.class);
        var buffers = new PoseStack();
        try (var render = mockStatic(RenderSystem.class); var upload = mockStatic(BufferUploader.class)) {
            render.when(RenderSystem::getModelViewStack).thenReturn(new PoseStack());
            var engine = new ParticleEngine(null, textures);
            var layers = new LinkedHashMap<ParticleRenderType, Queue<Particle>>();
            layers.put(flame, recordingParticle("flame", draws));
            layers.put(other, recordingParticle("other", draws));
            layers.put(core, recordingParticle("core", draws));
            var nativeLayers = ParticleEngine.class.getDeclaredField("particles");
            nativeLayers.setAccessible(true);
            nativeLayers.set(engine, layers);

            engine.render(buffers, null, mock(LightTexture.class), new Camera(), 0.5F, null);
            assertEquals(List.of("core", "flame", "other"), draws);
            assertFalse(Tesselator.getInstance().getBuilder().building());
            upload.verifyNoInteractions();
        }
    }

    @Test
    void customSheetDrawRestoresCullingDepthAndFabulousFramebufferEvenWhenDrawingThrows() throws Exception {
        var minecraft = mock(Minecraft.class);
        var renderer = mock(LevelRenderer.class);
        var main = mock(RenderTarget.class);
        var particles = mock(RenderTarget.class);
        var framebufferOrder = new ArrayList<String>();
        when(minecraft.getMainRenderTarget()).thenReturn(main);
        when(renderer.getParticlesTarget()).thenReturn(particles);
        var rendererField = Minecraft.class.getDeclaredField("levelRenderer");
        rendererField.setAccessible(true);
        rendererField.set(minecraft, renderer);
        doAnswer(call -> {
            framebufferOrder.add("main");
            return null;
        }).when(main).bindWrite(false);
        doAnswer(call -> {
            framebufferOrder.add("particles");
            return null;
        }).when(particles).bindWrite(false);
        var drawFailure = new IllegalStateException("fixture draw failure");
        var core = TTParticleLayers.additive(TTParticleSheets.sheet("nitor_core"));
        var ordinary = TTParticleLayers.additiveNoDepth(TTParticleSheets.sheet("sparkle"));
        var depthTest = new AtomicBoolean(true);
        var tesselator = mock(Tesselator.class);
        var builder = mock(BufferBuilder.class);
        var recordedGeometry = mock(BufferBuilder.RenderedBuffer.class);
        when(tesselator.getBuilder()).thenReturn(builder);
        when(builder.building()).thenReturn(true);
        when(builder.endOrDiscardIfEmpty()).thenReturn(recordedGeometry);
        try (var client = mockStatic(Minecraft.class); var iris = mockStatic(IrisCompat.class);
                var upload = mockStatic(BufferUploader.class); var render = mockStatic(RenderSystem.class);
                var gl = mockStatic(GlStateManager.class)) {
            client.when(Minecraft::getInstance).thenReturn(minecraft);
            client.when(Minecraft::useShaderTransparency).thenReturn(true);
            iris.when(IrisCompat::overridesShaders).thenReturn(false);
            gl.when(() -> GlStateManager._getInteger(GL11C.GL_CULL_FACE)).thenReturn(1);
            render.when(RenderSystem::enableDepthTest).thenAnswer(call -> {
                depthTest.set(true);
                return null;
            });
            render.when(RenderSystem::disableDepthTest).thenAnswer(call -> {
                depthTest.set(false);
                return null;
            });
            render.when(RenderSystem::disableCull).thenAnswer(call -> {
                framebufferOrder.add("disable cull");
                return null;
            });
            render.when(RenderSystem::enableCull).thenAnswer(call -> {
                framebufferOrder.add("enable cull");
                return null;
            });
            upload.when(() -> BufferUploader.drawWithShader(recordedGeometry)).thenAnswer(call -> {
                framebufferOrder.add(depthTest.get() ? "draw with depth" : "draw without depth");
                throw drawFailure;
            });

            assertSame(drawFailure, assertThrows(IllegalStateException.class, () -> ordinary.end(tesselator)));
            assertEquals(List.of("disable cull", "draw without depth", "enable cull"), framebufferOrder);
            assertTrue(depthTest.get());
            framebufferOrder.clear();
            assertSame(drawFailure, assertThrows(IllegalStateException.class, () -> core.end(tesselator)));
            assertEquals(List.of("main", "disable cull", "draw with depth", "enable cull", "particles"), framebufferOrder);
            framebufferOrder.clear();
            gl.when(() -> GlStateManager._getInteger(GL11C.GL_CULL_FACE)).thenReturn(0);
            assertSame(drawFailure, assertThrows(IllegalStateException.class, () -> ordinary.end(tesselator)));
            assertEquals(List.of("draw without depth"), framebufferOrder);
        }
    }

    private static Queue<Particle> recordingParticle(String name, List<String> draws) {
        var particle = mock(Particle.class);
        doAnswer(call -> {
            draws.add(name);
            return null;
        }).when(particle).render(any(), any(), anyFloat());
        return new ArrayDeque<>(List.of(particle));
    }
}
