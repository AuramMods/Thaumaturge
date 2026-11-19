package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;

public final class ModelPolygonCollector implements VertexConsumer {
    private static final int VERTICES_PER_POLYGON = 4;

    private final List<Vertex[]> polygons = new ArrayList<>();
    private Vertex[] pending = new Vertex[VERTICES_PER_POLYGON];
    private int pendingCount;
    private float x;
    private float y;
    private float z;
    private float u;
    private float v;

    public List<Vertex[]> polygons() {
        return polygons;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public void endVertex() {
        pending[pendingCount++] = new Vertex(x, y, z, u, v);
        if (pendingCount == VERTICES_PER_POLYGON) {
            polygons.add(pending);
            pending = new Vertex[VERTICES_PER_POLYGON];
            pendingCount = 0;
        }
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer normal(float normalX, float normalY, float normalZ) {
        return this;
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {}

    @Override
    public void unsetDefaultColor() {}

    public record Vertex(float x, float y, float z, float u, float v) {}
}
