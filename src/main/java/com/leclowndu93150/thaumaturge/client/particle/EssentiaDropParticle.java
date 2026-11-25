package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.compat.Colors.ARGB32;
import com.leclowndu93150.thaumaturge.compat.auram.AuramVisualSources;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.particle.EssentiaDropParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.phys.Vec3;

public final class EssentiaDropParticle extends TTQuadParticle implements EssentiaParticles.Source {
    private final int color;
    private final float baseAlpha;
    private final float baseScale;
    private volatile EssentiaParticles.Sample lightSample;

    private EssentiaDropParticle(
            ClientLevel level, double x, double y, double z, EssentiaDropParticleOptions data) {
        super(
                level,
                x,
                y,
                z,
                level.getRandom().nextGaussian() * 0.005,
                level.getRandom().nextGaussian() * 0.005,
                level.getRandom().nextGaussian() * 0.005);
        this.color = data.color();
        this.rCol = ARGB32.red(data.color()) / 255.0F;
        this.gCol = ARGB32.green(data.color()) / 255.0F;
        this.bCol = ARGB32.blue(data.color()) / 255.0F;
        this.baseAlpha = data.alpha();
        this.alpha = data.alpha();
        this.lifetime = 20 + level.getRandom().nextInt(10);
        this.baseScale = 0.4F + level.getRandom().nextFloat() * 0.2F;
        this.quadSize = this.baseScale;
        this.gravity = 0.01F;
        this.hasPhysics = true;
        this.friction = 0.98F;
        updateLightSample();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.yd -= 0.04 * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        float fade = 1.0F - (float) this.age / this.lifetime;
        this.alpha = this.baseAlpha * fade;
        this.quadSize = this.baseScale * (0.5F + fade * 0.5F);
        updateLightSample();
    }

    private void updateLightSample() {
        if (AuramVisualSources.available()) {
            this.lightSample = new EssentiaParticles.Sample(new Vec3(this.x, this.y, this.z), this.color, this.alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return EssentiaParticles.DROP_LAYER;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if (!IrisCompat.shadersActive()) super.render(buffer, camera, partialTick);
    }

    @Override
    public void renderEssentia(VertexConsumer buffer, Camera camera, float partialTick) {
        super.render(buffer, camera, partialTick);
    }

    @Override
    public EssentiaParticles.Sample lightSample() {
        return this.lightSample;
    }

    @Override
    protected float getU0() {
        return 0.0F;
    }

    @Override
    protected float getU1() {
        return 1.0F;
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }

    public static final class Provider implements ParticleProvider<EssentiaDropParticleOptions> {
        @Override
        public Particle createParticle(
                EssentiaDropParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xAux,
                double yAux,
                double zAux) {
            return new EssentiaDropParticle(level, x, y, z, options);
        }
    }
}
