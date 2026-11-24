package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.mixin.client.renderer.RenderStateShardAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11C;
import org.slf4j.Logger;

public final class ParticleSheet {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PNG_DIMENSION_OFFSET = 16;

    // Set on the render thread only while the shader is applied and the sheet is drawn.
    private static Mode shaderPackDrawMode;

    public enum Mode {
        ADDITIVE(true, true, true),
        ADDITIVE_SOFT(true, false, true),
        TRANSLUCENT(false, true, true),
        ADDITIVE_NO_DEPTH(true, false, false),
        TRANSLUCENT_NO_DEPTH(false, false, false);

        final boolean additive;
        final boolean depthMask;
        final boolean depthTest;

        Mode(boolean additive, boolean depthMask, boolean depthTest) {
            this.additive = additive;
            this.depthMask = depthMask;
            this.depthTest = depthTest;
        }
    }

    private final String name;
    private final ResourceLocation texture;
    private final Map<Mode, ParticleRenderType> layers = new ConcurrentHashMap<>();
    private volatile int frames;

    ParticleSheet(String name, ResourceLocation texture) {
        this.name = name;
        this.texture = texture;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public int frames() {
        int resolved = frames;
        if (resolved == 0) {
            resolved = readFrameCount();
            frames = resolved;
        }
        return resolved;
    }

    public float u0(int frame) {
        return (float) frame / frames();
    }

    public float u1(int frame) {
        return (float) (frame + 1) / frames();
    }

    ParticleRenderType layer(Mode mode) {
        return layers.computeIfAbsent(mode, this::build);
    }

    public static boolean isShaderPackDraw() {
        return shaderPackDrawMode != null;
    }

    public static boolean isAdditiveShaderPackDraw() {
        return shaderPackDrawMode != null && shaderPackDrawMode.additive;
    }

    private ParticleRenderType build(Mode mode) {
        return new ParticleRenderType() {
            @Override
            public void begin(BufferBuilder buffer, TextureManager textures) {
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            @Override
            public void end(Tesselator tesselator) {
                BufferBuilder builder = tesselator.getBuilder();
                // AsyncParticles calls end with a dummy builder to discover the sheet's format.
                if (!builder.building()) {
                    return;
                }
                var rendered = builder.endOrDiscardIfEmpty();
                if (rendered == null) {
                    return;
                }
                boolean shaderPack = IrisCompat.overridesShaders() && !IrisCompat.isShadowPass();
                // Fabulous composites its particle target with alpha, which dims the world behind Nitor.
                var target = !shaderPack && Minecraft.useShaderTransparency()
                        && TTParticleLayers.usesNitorAdditiveBlend(this)
                        ? RenderStateShardAccessor.thaumaturge$particlesTarget() : null;
                if (target != null) {
                    target.clearRenderState();
                }
                // Forge culls the back of these quads with the source winding and facing modes.
                boolean restoreCull = GlStateManager._getInteger(GL11C.GL_CULL_FACE) != 0;
                if (restoreCull) {
                    RenderSystem.disableCull();
                }
                Mode previousMode = shaderPackDrawMode;
                shaderPackDrawMode = shaderPack ? mode : null;
                try {
                    RenderSystem.setShader(shaderPack
                            ? IrisCompat::particleTranslucentShader : GameRenderer::getParticleShader);
                    RenderSystem.setShaderTexture(0, texture);
                    RenderSystem.enableBlend();
                    if (!mode.additive) {
                        RenderSystem.defaultBlendFunc();
                    } else if (shaderPack) {
                        // Accumulate coverage for the shader pack's composite while adding RGB light.
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
                                GlStateManager.DestFactor.ONE);
                    } else {
                        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    }
                    if (mode.depthTest) {
                        RenderSystem.enableDepthTest();
                    } else {
                        RenderSystem.disableDepthTest();
                    }
                    RenderSystem.depthMask(mode.depthMask);
                    BufferUploader.drawWithShader(rendered);
                } finally {
                    shaderPackDrawMode = previousMode;
                    if (!mode.depthTest) {
                        RenderSystem.enableDepthTest();
                    }
                    if (restoreCull) {
                        RenderSystem.enableCull();
                    }
                    if (target != null) {
                        target.setupRenderState();
                    }
                }
            }

            @Override
            public String toString() {
                return name + "[" + mode + "]";
            }
        };
    }

    void invalidate() {
        frames = 0;
        layers.clear();
    }

    private int readFrameCount() {
        try (InputStream stream = Minecraft.getInstance()
                        .getResourceManager()
                        .getResourceOrThrow(texture)
                        .open();
                DataInputStream data = new DataInputStream(stream)) {
            data.skipBytes(PNG_DIMENSION_OFFSET);
            int width = data.readInt();
            int height = data.readInt();
            return Math.max(1, width / Math.max(1, height));
        } catch (IOException e) {
            LOGGER.error("Cannot read particle sheet {}", texture, e);
            return 1;
        }
    }
}
