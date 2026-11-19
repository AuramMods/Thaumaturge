package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class RiftOutlineTest {
    @Test
    void outlineIsSeedDeterministic() {
        List<Vec3> firstPoints = new ArrayList<>();
        List<Float> firstWidths = new ArrayList<>();
        List<Vec3> secondPoints = new ArrayList<>();
        List<Float> secondWidths = new ArrayList<>();
        RiftOutline.build(24, 42, firstPoints, firstWidths);
        RiftOutline.build(24, 42, secondPoints, secondWidths);
        assertEquals(firstPoints, secondPoints);
        assertEquals(firstWidths, secondWidths);
        assertEquals(firstPoints.size(), firstWidths.size());
    }
}
