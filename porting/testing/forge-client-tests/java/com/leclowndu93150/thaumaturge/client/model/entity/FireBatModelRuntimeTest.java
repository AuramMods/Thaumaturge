package com.leclowndu93150.thaumaturge.client.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.IBatAnimated;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FireBatModelRuntimeTest {
    private static final VertexFormat FORMAT = DefaultVertexFormat.NEW_ENTITY;

    @Test
    void sharedModelResetsBetweenFlightHangingAndAnotherBatsTakeoff() {
        var root = FireBatModel.createLayer().bakeRoot();
        var model = new FireBatModel<BatProbe>(root);
        var body = root.getChild("body");
        var head = root.getChild("head");
        var rightWing = body.getChild("right_wing");
        var leftWing = body.getChild("left_wing");
        var flight = new AnimationState();
        flight.start(20);
        var rest = new AnimationState();
        var bat = mock(BatProbe.class);
        when(bat.flyAnimation()).thenReturn(flight);
        when(bat.restAnimation()).thenReturn(rest);

        model.setupAnim(bat, 0, 0, 20, 0, 0);
        float firstStroke = rightWing.zRot;
        float flightSpan = worldSpaceBounds(model).width();
        model.setupAnim(bat, 0, 0, 22.5F, 0, 0);
        assertTrue(rightWing.zRot > firstStroke + 0.7F);
        assertEquals(-rightWing.zRot, leftWing.zRot, 1.0e-6F);
        model.setupAnim(bat, 0, 0, 30, 0, 0);
        assertEquals(firstStroke, rightWing.zRot, 1.0e-6F);

        when(bat.isResting()).thenReturn(true);
        rest.start(40);
        model.setupAnim(bat, 0, 0, 40, 35, 0);
        float settlingWingYaw = rightWing.yRot;
        model.setupAnim(bat, 0, 0, 43, 35, 0);
        assertTrue(rightWing.yRot < settlingWingYaw);
        assertEquals(-rightWing.yRot, leftWing.yRot, 1.0e-6F);
        assertEquals(Mth.PI, body.xRot);
        assertEquals(35.0F * Mth.DEG_TO_RAD, head.yRot);
        Bounds hanging = worldSpaceBounds(model);
        assertTrue(hanging.width() < flightSpan * 0.75F);
        assertEquals(0.9F, hanging.top(), 0.03F);

        var spellBat = mock(BatProbe.class);
        var spellFlight = new AnimationState();
        spellFlight.start(8);
        when(spellBat.flyAnimation()).thenReturn(spellFlight);
        model.setupAnim(spellBat, 0, 0, 8, 0, 0);
        assertEquals(firstStroke, rightWing.zRot, 1.0e-6F);
        assertEquals(0.0F, head.yRot);
        assertEquals(500, flight.getAccumulatedTime());
        assertEquals(150, rest.getAccumulatedTime());
    }

    @ParameterizedTest
    @ValueSource(strings = {"firebat", "spellbat"})
    void everyModelFaceUsesPaintedPixelsInTheShippedAtlas(String texture) throws IOException {
        var model = new FireBatModel<BatProbe>(FireBatModel.createLayer().bakeRoot());
        var path = Path.of(System.getProperty("thaumaturge.testResourceRoot"),
                "assets", "thaumaturge", "textures", "entity", texture + ".png");
        var buffer = new BufferBuilder(4096);
        buffer.begin(VertexFormat.Mode.QUADS, FORMAT);
        model.renderToBuffer(new PoseStack(), buffer, 0, 0, 1, 1, 1, 1);
        var rendered = buffer.end();
        try (var stream = Files.newInputStream(path); var atlas = NativeImage.read(stream)) {
            assertEquals(32, atlas.getWidth());
            assertEquals(32, atlas.getHeight());
            assertEquals(104, rendered.drawState().vertexCount());
            var vertices = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            for (int vertex = 0; vertex < rendered.drawState().vertexCount(); vertex += 4) {
                verifyPaintedQuad(vertices, vertex, atlas);
            }
        } finally {
            rendered.release();
        }
    }

    @Test
    void modelSupportsTheFireBatCutoutAndSpellBatTranslucentRenderTypes() {
        var fireTexture = TTIds.rl("textures/entity/firebat.png");
        var spellTexture = TTIds.rl("textures/entity/spellbat.png");
        var layer = FireBatModel.createLayer();
        var fire = new FireBatModel<BatProbe>(layer.bakeRoot());
        var spell = new FireBatModel<BatProbe>(layer.bakeRoot(), RenderType::entityTranslucent);

        assertSame(RenderType.entityCutoutNoCull(fireTexture), fire.renderType(fireTexture));
        assertSame(RenderType.entityTranslucent(spellTexture), spell.renderType(spellTexture));
    }

    private static Bounds worldSpaceBounds(FireBatModel<?> model) {
        var pose = new PoseStack();
        // The transforms in Forge's LivingEntityRenderer convert model coordinates to world coordinates.
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.501F, 0.0F);
        var buffer = new BufferBuilder(4096);
        buffer.begin(VertexFormat.Mode.QUADS, FORMAT);
        model.renderToBuffer(pose, buffer, 0, 0, 1, 1, 1, 1);
        var rendered = buffer.end();
        try {
            var vertices = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            float minX = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            for (int vertex = 0; vertex < rendered.drawState().vertexCount(); vertex++) {
                int offset = vertex * FORMAT.getVertexSize();
                minX = Math.min(minX, vertices.getFloat(offset));
                maxX = Math.max(maxX, vertices.getFloat(offset));
                maxY = Math.max(maxY, vertices.getFloat(offset + Float.BYTES));
            }
            return new Bounds(maxX - minX, maxY);
        } finally {
            rendered.release();
        }
    }

    private static void verifyPaintedQuad(ByteBuffer vertices, int first, NativeImage atlas) {
        int minU = atlas.getWidth();
        int minV = atlas.getHeight();
        int maxU = 0;
        int maxV = 0;
        for (int corner = 0; corner < 4; corner++) {
            int offset = (first + corner) * FORMAT.getVertexSize() + FORMAT.getOffset(2);
            int u = Math.round(vertices.getFloat(offset) * atlas.getWidth());
            int v = Math.round(vertices.getFloat(offset + Float.BYTES) * atlas.getHeight());
            minU = Math.min(minU, u);
            maxU = Math.max(maxU, u);
            minV = Math.min(minV, v);
            maxV = Math.max(maxV, v);
        }
        assertTrue(minU >= 0 && maxU <= atlas.getWidth() && minU < maxU);
        assertTrue(minV >= 0 && maxV <= atlas.getHeight() && minV < maxV);
        for (int v = minV; v < maxV; v++) {
            for (int u = minU; u < maxU; u++) {
                if (atlas.getPixelRGBA(u, v) >>> 24 != 0) {
                    return;
                }
            }
        }
        throw new AssertionError("No painted pixels for face starting at vertex " + first);
    }

    private record Bounds(float width, float top) {}

    public abstract static class BatProbe extends Mob implements IBatAnimated {
        protected BatProbe(EntityType<? extends Mob> type, Level level) {
            super(type, level);
        }
    }
}
