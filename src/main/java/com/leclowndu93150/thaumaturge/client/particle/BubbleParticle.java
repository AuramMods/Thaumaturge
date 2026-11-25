package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.compat.auram.AuramVisualSources;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;

public final class BubbleParticle extends TTParticle implements EssentiaParticles.Source {
    private static final int FRAME_COUNT = 4;
    private static final int BUBBLE_FRAME = 0;
    private static final int POP_FRAME_1 = 1;
    private static final int POP_FRAME_2 = 2;
    private static final int DROPLET_FRAME = 3;
    private static final int POP_TICKS = 2;
    private static final float DRIFT = 0.001F;
    private static final ParticleSheet SHEET = TTParticleSheets.sheet("bubble");

    private final int holdFrame;
    private final float startSize;
    private final float endSize;
    private final boolean essentia;
    private final int color;
    private volatile EssentiaParticles.Sample lightSample;

    private BubbleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            BubbleParticleOptions options,
            boolean essentia) {
        super(level, x, y, z, vx, vy, vz, SHEET);
        this.essentia = essentia;
        this.color = options.color();
        setColor(options.color());
        this.alpha = options.alpha();
        this.lifetime = Math.max(POP_TICKS + 1, options.age());
        this.gravity = options.buoyancy();
        this.startSize = options.scale() * 0.1F;
        this.quadSize = this.startSize;
        this.endSize = this.startSize / 2.0F;
        this.holdFrame = options.droplet() ? DROPLET_FRAME : BUBBLE_FRAME;
        frame(this.holdFrame);
        updateLightSample();
    }

    public static Particle flowingEssentia(
            ClientLevel level, Vec3 point, double vx, double vy, double vz, BubbleParticleOptions options) {
        return new BubbleParticle(level, point.x, point.y, point.z, vx, vy, vz, options, true);
    }

    @Override
    public void tick() {
        super.tick();
        updateLightSample();
    }

    private void updateLightSample() {
        if (this.essentia && AuramVisualSources.available()) {
            this.lightSample = new EssentiaParticles.Sample(new Vec3(this.x, this.y, this.z), this.color, this.alpha);
        }
    }

    @Override
    protected void update() {
        drift(DRIFT, DRIFT, DRIFT);
        int remaining = this.lifetime - this.age;
        if (remaining <= 0) {
            frame(POP_FRAME_2);
        } else if (remaining == 1) {
            frame(POP_FRAME_1);
        } else {
            frame(this.holdFrame);
        }
        this.quadSize = Keyframes.sample(progress(), this.startSize, this.endSize);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.essentia ? EssentiaParticles.FLOW_LAYER : TTParticleLayers.translucent(this.sheet);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if (!this.essentia || !IrisCompat.shadersActive()) super.render(buffer, camera, partialTick);
    }

    @Override
    public void renderEssentia(VertexConsumer buffer, Camera camera, float partialTick) {
        super.render(buffer, camera, partialTick);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return this.essentia ? LightTexture.FULL_BRIGHT : super.getLightColor(partialTick);
    }

    @Override
    public EssentiaParticles.Sample lightSample() {
        return this.lightSample;
    }

    public static final class Provider implements ParticleProvider<BubbleParticleOptions> {
        @Override
        public Particle createParticle(
                BubbleParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new BubbleParticle(level, x, y, z, vx, vy, vz, options, false);
        }
    }
}
