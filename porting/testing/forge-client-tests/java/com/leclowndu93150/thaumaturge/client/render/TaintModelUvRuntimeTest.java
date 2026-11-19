package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.client.taint.overlay.ModelRootParts;
import com.leclowndu93150.thaumaturge.client.taint.overlay.pattern.ModelUvLayout;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class TaintModelUvRuntimeTest {
    @Test
    void nativeCubeSuppliesSixUvFacesInTheInitialPartPose() {
        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(3, 4).addBox(0, 0, 0, 4, 6, 8), PartPose.offset(3, 7, 11));
        ModelPart root = LayerDefinition.create(mesh, 64, 32).bakeRoot();
        root.getChild("body").setPos(40, 50, 60);

        ModelUvLayout layout = ModelUvLayout.of(new CubeModel(root));

        assertEquals(64, layout.width());
        assertEquals(32, layout.height());
        assertEquals(6, layout.faces().size());
        var min = new Vector3f(Float.POSITIVE_INFINITY);
        var max = new Vector3f(Float.NEGATIVE_INFINITY);
        for (var face : layout.faces()) {
            var rect = face.rect();
            assertTrue(rect.width() > 0 && rect.height() > 0);
            assertEquals(1.0F, face.normal().length(), 1.0E-6F);
            for (int u : new int[] {rect.x(), rect.x() + rect.width()}) {
                for (int v : new int[] {rect.y(), rect.y() + rect.height()}) {
                    Vector3f point = face.point(u, v, new Vector3f());
                    min.min(point);
                    max.max(point);
                }
            }
        }
        assertTrue(min.equals(new Vector3f(3, 7, 11), 1.0E-5F), min::toString);
        assertTrue(max.equals(new Vector3f(7, 13, 19), 1.0E-5F), max::toString);
    }

    private static final class CubeModel extends Model implements ModelRootParts {
        private final ModelPart root;

        private CubeModel(ModelPart root) {
            super(RenderType::entityCutout);
            this.root = root;
        }

        @Override
        public Iterable<ModelPart> thaumaturge$rootParts() {
            return List.of(root);
        }

        @Override
        public void renderToBuffer(PoseStack poses, VertexConsumer vertices, int light, int overlay,
                float red, float green, float blue, float alpha) {
            root.render(poses, vertices, light, overlay, red, green, blue, alpha);
        }
    }
}
