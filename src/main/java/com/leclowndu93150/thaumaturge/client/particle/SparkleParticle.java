package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.mixin.client.particle.ParticleEngineAccessor;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SparkleParticle extends TTParticle {
    private static final ParticleSheet SHEET = TTParticleSheets.sheet("sparkle");
    // Keep the particle engine queue addressable across shader and resource reloads.
    private static final ParticleRenderType LAYER = TTParticleLayers.additive(SHEET);
    private static final int LOOP_FRAMES = 16;
    private static final int LOW_LOOP_START = 0;
    private static final int HIGH_LOOP_START = 16;
    private static final int HIGH_LOOP_ODDS = 5;
    private static final float SIZE_UNIT = 0.1F;
    private static final float GROWTH = 2.0F;
    private static final int LIFE_FACTOR = 4;
    private static final int TICKS_PER_FLICKER = 4;
    private static final int FLICKER_KEY_PADDING = 2;
    private static final float FLICKER_CUTOFF_PROGRESS = 0.85F;
    private static final float HORIZONTAL_NUDGE = 5.0E-4F;
    private static final float VERTICAL_NUDGE = 0.001F;
    private static final double WIND_SCALE = 5.0E-4;

    private final boolean flicker;
    private final int loopStart;
    private final float baseSize;
    private final float[] flickerKeys;

    private SparkleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            SparkleParticleOptions options) {
        super(level, x, y, z, vx, vy, vz, SHEET);
        setColor(options.color());
        this.flicker = options.flicker();
        this.lifetime = lifetimeFor(options.baseAge(), this.random);
        this.friction = options.decay();
        this.gravity = options.gravity();
        this.baseSize = options.scale() * SIZE_UNIT;
        this.quadSize = this.baseSize;
        this.alpha = 0.0F;
        this.loopStart = this.random.nextInt(HIGH_LOOP_ODDS) == 0 ? HIGH_LOOP_START : LOW_LOOP_START;
        this.flickerKeys = this.flicker ? buildFlickerKeys(this.lifetime, this.random) : new float[0];
        frame(this.loopStart);
        setDelay(options.delay());
        setMoonWind(WIND_SCALE);
    }

    static int lifetimeFor(int requestedBaseAge, RandomSource random) {
        int baseAge = Math.max(1, requestedBaseAge);
        return baseAge * LIFE_FACTOR + random.nextInt(baseAge);
    }

    static float[] buildFlickerKeys(int lifetime, RandomSource random) {
        float[] keys = new float[lifetime / TICKS_PER_FLICKER + FLICKER_KEY_PADDING];
        keys[0] = 1.0F;
        for (int i = 1; i < keys.length; i++) {
            float previousProgress = (float) ((i - 1) * TICKS_PER_FLICKER) / lifetime;
            keys[i] = i >= 2 && previousProgress > FLICKER_CUTOFF_PROGRESS ? 0.0F : random.nextFloat();
        }
        return keys;
    }

    static float flickerAlpha(int age, float[] keys) {
        int segment = Math.min(age / TICKS_PER_FLICKER, keys.length - 2);
        float withinSegment = (float) (age - segment * TICKS_PER_FLICKER) / TICKS_PER_FLICKER;
        return Mth.lerp(withinSegment, keys[segment], keys[segment + 1]);
    }

    @Override
    protected void update() {
        frame(this.loopStart + this.age % LOOP_FRAMES);
        float t = progress();
        drift(HORIZONTAL_NUDGE, VERTICAL_NUDGE, HORIZONTAL_NUDGE);
        if (this.flicker) {
            this.alpha = flickerAlpha(this.age, this.flickerKeys);
            this.quadSize = Mth.lerp(t, this.baseSize, this.baseSize * GROWTH);
        } else {
            this.alpha = Keyframes.sample(t, 0.0F, 1.0F, 0.0F);
            this.quadSize = this.baseSize * Keyframes.sample(t, 1.0F, GROWTH, 1.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return LAYER;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if (!IrisCompat.shadersActive()) super.render(buffer, camera, partialTick);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return IrisCompat.shadersActive() ? LightTexture.FULL_BRIGHT : super.getLightColor(partialTick);
    }

    private void renderAfterLevel(VertexConsumer buffer, Camera camera, float partialTick) {
        super.render(buffer, camera, partialTick);
    }

    public static void renderAfterLevel(ParticleEngine engine, MultiBufferSource.BufferSource buffers,
            Camera camera, float partialTick) {
        var particles = ((ParticleEngineAccessor) engine).thaumaturge$particles().get(LAYER);
        if (particles == null || particles.isEmpty()) return;
        var type = TTRenderTypes.fxAlphaAdditive(SHEET.texture());
        VertexConsumer buffer = buffers.getBuffer(type);
        for (Particle particle : particles) {
            if (particle instanceof SparkleParticle sparkle && sparkle.isAlive()) {
                sparkle.renderAfterLevel(buffer, camera, partialTick);
            }
        }
        buffers.endBatch(type);
    }

    public static final class Provider implements ParticleProvider<SparkleParticleOptions> {
        @Override
        public Particle createParticle(
                SparkleParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new SparkleParticle(level, x, y, z, vx, vy, vz, options);
        }
    }
}
