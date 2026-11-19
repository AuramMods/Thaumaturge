package com.leclowndu93150.thaumaturge.client.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.screen.widget.TTSlider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import org.junit.jupiter.api.Test;

class SliderInputRuntimeTest {
    @Test
    void forgePrimaryClickAndDragUpdateBothSliderOrientations() {
        var changes = new ArrayList<Float>();
        var horizontal = new TTSlider(10, 20, 40, 8, false, 2.0F, 10.0F, 2.0F, changes::add);
        var vertical = new TTSlider(10, 20, 8, 40, true, 2.0F, 10.0F, 2.0F, changes::add);
        var minecraft = mock(Minecraft.class);
        when(minecraft.getSoundManager()).thenReturn(mock(SoundManager.class));
        try (var instance = mockStatic(Minecraft.class)) {
            instance.when(Minecraft::getInstance).thenReturn(minecraft);

            assertFalse(horizontal.mouseClicked(30, 24, 1));
            assertFalse(horizontal.mouseClicked(9, 24, 0));
            assertTrue(horizontal.mouseClicked(30, 24, 0));
            assertTrue(horizontal.mouseDragged(60, 24, 0, 30, 0));
            assertTrue(vertical.mouseClicked(14, 40, 0));
            assertTrue(vertical.mouseDragged(14, 10, 0, 0, -30));
        }
        assertEquals(List.of(6.0F, 10.0F, 6.0F, 2.0F), changes);
    }
}
