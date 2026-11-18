package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import com.leclowndu93150.thaumaturge.content.particle.TTColorParticleOptions;
import net.minecraft.util.RandomSource;

public final class LeafMoteParticle extends TTParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BASE_LIFETIME = 4;
    private static final float ALPHA = 0.6F;

    private LeafMoteParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            TTColorParticleOptions options,
            ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.alpha = ALPHA;
        this.lifetime = BASE_LIFETIME + this.random.nextInt(4);
        this.quadSize = (0.8F + this.random.nextFloat() * 0.3F) * 0.1F;
    }

    @Override
    protected void update() {
        frame(this.age % FRAME_COUNT);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<TTColorParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("leaf_mote");

        @Override
        public Particle createParticle(
                TTColorParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            RandomSource random = level.getRandom();
            return new LeafMoteParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
