package com.leclowndu93150.thaumaturge.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

abstract class TTBreakingItemParticle extends BreakingItemParticle {
    protected TTBreakingItemParticle(ClientLevel level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        var origin = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - origin.x);
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - origin.y);
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - origin.z);
        var rotation = camera.rotation();
        if (this.roll != 0.0F) {
            rotation = new Quaternionf(rotation).rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
        }
        ParticleQuads.draw(buffer, rotation, x, y, z, getQuadSize(partialTick),
                getU0(), getU1(), getV0(), getV1(), this.rCol, this.gCol, this.bCol, this.alpha, getLightColor(partialTick));
    }
}
