package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import com.leclowndu93150.thaumaturge.compat.Colors.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class SmokeSpiralParticle extends TTParticle {
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 10;
    private static final float SIZE = 0.15F;
    private static final float ALPHA_SCALE = 0.66F;
    private static final double TURNS = 2.0;
    private static final int ACTIVE_FRAMES = 4;
    private static final double FLOOR_CLEARANCE = 0.1;

    private final double radius;
    private final double startAngle;
    private final double floorY;

    private SmokeSpiralParticle(
            ClientLevel level, double x, double y, double z, SmokeSpiralParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.gravity = -0.01F;
        this.radius = options.radius();
        this.startAngle = Math.toRadians(options.start());
        this.floorY = options.minY() + FLOOR_CLEARANCE;
        setColor(
                Mth.clamp(options.r(), 0.0F, 1.0F),
                Mth.clamp(options.g(), 0.0F, 1.0F),
                Mth.clamp(options.b(), 0.0F, 1.0F));
        this.quadSize = SIZE;
        this.setSize(0.01F, 0.01F);
        this.alpha = ALPHA_SCALE;
        frame(0);
    }

    @Override
    protected void update() {
        float progress = progress();
        this.alpha = ALPHA_SCALE * (1.0F - progress);
        frame(frameAt(progress));
    }

    static int frameAt(float progress) {
        return Math.min((int) (Mth.clamp(progress, 0.0F, 1.0F) * ACTIVE_FRAMES), ACTIVE_FRAMES - 1);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        double t = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        double climb = Math.PI * t;
        double ring = this.radius * Math.sin(climb);
        double azimuth = this.startAngle + TURNS * Math.PI * 2.0 * t;
        Vec3 cam = camera.getPosition();
        double centerX = Mth.lerp(partialTick, this.xo, this.x);
        double centerY = Mth.lerp(partialTick, this.yo, this.y);
        double centerZ = Mth.lerp(partialTick, this.zo, this.z);
        double offsetY = -this.radius * Math.cos(climb);
        double clampedOffsetY = Math.max(this.floorY, centerY + offsetY) - centerY;
        Quaternionf rotation = new Quaternionf();
        getFacingCameraMode().setRotation(rotation, camera, partialTick);
        float px = (float) (centerX - Math.sin(azimuth) * ring - cam.x());
        float py = (float) (centerY + clampedOffsetY - cam.y());
        float pz = (float) (centerZ + Math.cos(azimuth) * ring - cam.z());
        int color = ARGB32.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol);
        int light = getLightColor(partialTick);
        float size = getQuadSize(partialTick);
        ParticleQuads.draw(buffer, rotation, px, py, pz, size, getU0(), getU1(), getV0(), getV1(), color, light);
    }

    public static final class Provider implements ParticleProvider<SmokeSpiralParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("smoke_spiral");

        @Override
        public Particle createParticle(
                SmokeSpiralParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new SmokeSpiralParticle(level, x, y, z, options, SHEET);
        }
    }
}
