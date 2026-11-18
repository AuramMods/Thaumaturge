package com.leclowndu93150.thaumaturge.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

abstract class TTQuadParticle extends SingleQuadParticle {
    @FunctionalInterface
    protected interface FacingCameraMode {
        FacingCameraMode LOOKAT_XYZ = (rotation, camera, partialTick) -> rotation.set(camera.rotation());

        void setRotation(Quaternionf rotation, Camera camera, float partialTick);
    }

    protected TTQuadParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz);
    }

    protected FacingCameraMode getFacingCameraMode() {
        return FacingCameraMode.LOOKAT_XYZ;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        var rotation = new Quaternionf();
        getFacingCameraMode().setRotation(rotation, camera, partialTick);
        if (this.roll != 0.0F) {
            rotation.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
        }
        renderRotatedQuad(buffer, camera, rotation, partialTick);
    }

    protected void renderRotatedQuad(VertexConsumer buffer, Camera camera, Quaternionf rotation, float partialTick) {
        var origin = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - origin.x);
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - origin.y);
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - origin.z);
        renderRotatedQuad(buffer, rotation, x, y, z, partialTick);
    }

    protected void renderRotatedQuad(
            VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float partialTick) {
        ParticleQuads.draw(buffer, rotation, x, y, z, getQuadSize(partialTick),
                getU0(), getU1(), getV0(), getV1(), this.rCol, this.gCol, this.bCol, this.alpha, getLightColor(partialTick));
    }
}
