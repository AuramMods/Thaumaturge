package com.leclowndu93150.thaumaturge.client.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class ResearchScreenRenderRuntimeTest {
    @Test
    void forgeDrawsTheResearchBackdropBeforeWidgetsWithTheSameFrameArguments() {
        List<String> calls = new ArrayList<>();
        var screen = new ProbeScreen(calls);

        screen.renderWithTooltip(mock(GuiGraphics.class), 17, 23, 0.25F);

        assertEquals(List.of("background", "backdrop 17 23 0.25", "widget 17 23 0.25"), calls);
    }

    private static final class ProbeScreen extends AbstractTTScreen {
        private final List<String> calls;

        private ProbeScreen(List<String> calls) {
            super(Component.empty());
            this.calls = calls;
            addRenderableOnly((graphics, x, y, frame) -> calls.add("widget " + x + " " + y + " " + frame));
        }

        @Override
        public void renderBackground(GuiGraphics graphics) {
            calls.add("background");
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
            calls.add("backdrop " + mouseX + " " + mouseY + " " + partialTick);
        }
    }
}
