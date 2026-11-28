package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.compat.auram.AuramVisualSources;
import com.leclowndu93150.thaumaturge.mixin.client.particle.ParticleEngineAccessor;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

/** The particle engine owns the lifetime and limit of both kinds of flying essentia. */
public final class EssentiaParticles {
    static final ParticleSheet DROP_SHEET = TTParticleSheets.sheet("essentia_drop");
    private static final ParticleSheet FLOW_SHEET = new ParticleSheet("flowing_essentia",
            TTParticleSheets.sheet("bubble").texture());
    // Keep the queue identities stable across resource reloads and apart from ordinary bubbles.
    static final ParticleRenderType DROP_LAYER = TTParticleLayers.translucent(DROP_SHEET);
    static final ParticleRenderType FLOW_LAYER = TTParticleLayers.translucent(FLOW_SHEET);
    private static final List<Layer> LAYERS = List.of(new Layer(DROP_SHEET, DROP_LAYER), new Layer(FLOW_SHEET, FLOW_LAYER));

    record Sample(Vec3 position, int color, float opacity) {}
    private record Layer(ParticleSheet sheet, ParticleRenderType type) {}

    interface Source {
        Sample lightSample();
        void renderEssentia(VertexConsumer buffer, Camera camera, float partialTick);
    }

    private EssentiaParticles() {}

    public static void collectVisualSources(ClientLevel level, ParticleEngine engine) {
        var queues = ((ParticleEngineAccessor) engine).thaumaturge$particles();
        for (Layer layer : LAYERS) {
            var particles = queues.get(layer.type());
            if (particles == null) continue;
            for (var particle : particles) {
                if (particle.isAlive() && particle instanceof Source source) {
                    Sample sample = source.lightSample();
                    if (sample != null) AuramVisualSources.essentia(level, sample.position(), sample.color(), sample.opacity());
                }
            }
        }
    }

    public static void renderAfterLevel(ParticleEngine engine, MultiBufferSource.BufferSource buffers,
            Camera camera, float partialTick) {
        var queues = ((ParticleEngineAccessor) engine).thaumaturge$particles();
        for (Layer layer : LAYERS) {
            var particles = queues.get(layer.type());
            if (particles == null || particles.isEmpty()) continue;
            var type = TTRenderTypes.fxTranslucent(layer.sheet().texture());
            VertexConsumer buffer = buffers.getBuffer(type);
            for (var particle : particles) {
                if (particle.isAlive() && particle instanceof Source source) {
                    source.renderEssentia(buffer, camera, partialTick);
                }
            }
            buffers.endBatch(type);
        }
    }
}
