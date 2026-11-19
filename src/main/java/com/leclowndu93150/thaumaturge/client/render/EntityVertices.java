package com.leclowndu93150.thaumaturge.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/** Finishes a native entity vertex with the source's transformed lighting direction. */
public final class EntityVertices {
    private EntityVertices() {}

    public static void finish(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z) {
        var position = pose.pose();
        if ((position.properties() & Matrix4fc.PROPERTY_ORTHONORMAL) != 0 && position.determinant3x3() > 0.0F) {
            buffer.normal(pose.normal(), x, y, z).endVertex();
            return;
        }

        // The position matrix retains the full scale and reflection lost by the older normal-matrix approximation.
        var direction = position.normal(new Matrix3f()).transform(x, y, z, new Vector3f());
        if (direction.lengthSquared() > 0.0F) {
            direction.normalize();
        }
        buffer.normal(direction.x, direction.y, direction.z).endVertex();
    }
}
