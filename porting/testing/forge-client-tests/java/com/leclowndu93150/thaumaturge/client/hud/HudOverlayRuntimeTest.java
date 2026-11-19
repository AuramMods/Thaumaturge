package com.leclowndu93150.thaumaturge.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.ModLoadingContext;
import org.junit.jupiter.api.Test;

class HudOverlayRuntimeTest {
    @Test
    @SuppressWarnings("removal")
    void nativeRegistrationKeepsNamespaceOrderAndTheOriginalFrameArguments() throws Exception {
        var fixture = new HudFixture();
        var vanillaOrder = new ArrayList<ResourceLocation>();
        var overlays = new HashMap<ResourceLocation, IGuiOverlay>();
        var vanilla = mock(IGuiOverlay.class);
        for (var overlay : VanillaGuiOverlay.values()) {
            vanillaOrder.add(overlay.id());
            overlays.put(overlay.id(), vanilla);
        }
        var names = new ArrayList<String>();
        var event = new RegisterGuiOverlaysEvent(overlays, vanillaOrder);
        try (var loadingContext = mockStatic(ModLoadingContext.class); var render = mockStatic(RenderSystem.class)) {
            var context = mock(ModLoadingContext.class);
            loadingContext.when(ModLoadingContext::get).thenReturn(context);
            when(context.getActiveNamespace()).thenReturn(TTIds.MODID);
            var anchor = VanillaGuiOverlay.ITEM_NAME.id();
            event.registerAbove(anchor, "hover_hud", GuiBlend.alphaBlendedOverlay(
                    (gui, graphics, partialTick, width, height) -> names.add("hover")));
            event.registerAbove(anchor, "radial_focus", GuiBlend.alphaBlendedOverlay(
                    (gui, graphics, partialTick, width, height) -> {
                        assertSame(fixture.gui, gui);
                        assertSame(fixture.graphics, graphics);
                        assertEquals(0.375F, partialTick);
                        assertEquals(320, width);
                        assertEquals(240, height);
                        names.add("radial");
                    }));
            int itemName = vanillaOrder.indexOf(anchor);
            assertTrue(vanillaOrder.indexOf(VanillaGuiOverlay.EXPERIENCE_BAR.id()) < itemName);
            assertTrue(itemName < vanillaOrder.indexOf(VanillaGuiOverlay.POTION_ICONS.id()));
            assertEquals(List.of(TTIds.rl("radial_focus"), TTIds.rl("hover_hud")),
                    vanillaOrder.subList(itemName + 1, itemName + 3));

            fixture.options.hideGui = true;
            overlays.get(TTIds.rl("radial_focus")).render(fixture.gui, fixture.graphics, 0.375F, 320, 240);
            verifyNoInteractions(fixture.graphics);
            render.verifyNoInteractions();
            assertTrue(names.isEmpty());

            fixture.options.hideGui = false;
            for (var id : vanillaOrder.subList(itemName + 1, itemName + 3)) {
                overlays.get(id).render(fixture.gui, fixture.graphics, 0.375F, 320, 240);
            }
            assertEquals(List.of("radial", "hover"), names);
            render.verify(RenderSystem::disableDepthTest, times(2));
            render.verify(RenderSystem::disableBlend, times(2));
        }
    }

    @Test
    void gaugeStackUsesOnlyVisibleAndUnclaimedGaugesWithNativePartialTicks() throws Exception {
        var fixture = new HudFixture();
        when(fixture.graphics.pose()).thenReturn(fixture.pose);
        var positions = new ArrayList<Float>();
        var first = gauge(true, "shared", 23, fixture, positions);
        var hidden = gauge(false, null, 30, fixture, positions);
        var claimed = gauge(true, "shared", 45, fixture, positions);
        var last = gauge(true, null, 15, fixture, positions);
        var stack = new LeftHudStack(List.of(first, hidden, claimed, last));
        stack.render(fixture.gui, fixture.graphics, 0.375F, 320, 240);
        assertEquals(List.of(0.0F, 23.0F), positions);
        assertTrue(fixture.pose.clear());
        assertEquals(0.0F, fixture.pose.last().pose().m31());

        var broken = mock(LeftHudStack.Gauge.class);
        when(broken.visible(fixture.minecraft, fixture.minecraft.player)).thenReturn(true);
        var failure = new IllegalStateException("broken gauge");
        doThrow(failure).when(broken).render(fixture.graphics, 0.375F);
        var throwingStack = new LeftHudStack(List.of(first, broken));
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> throwingStack.render(fixture.gui, fixture.graphics, 0.375F, 320, 240)));
        assertTrue(fixture.pose.clear());
        assertEquals(0.0F, fixture.pose.last().pose().m31());
    }

    private static LeftHudStack.Gauge gauge(
            boolean visible, String group, int height, HudFixture fixture, List<Float> positions) {
        return new LeftHudStack.Gauge() {
            @Override
            public boolean visible(Minecraft mc, LocalPlayer player) {
                assertSame(fixture.minecraft, mc);
                assertSame(fixture.minecraft.player, player);
                return visible;
            }

            @Override
            public int height() {
                return height;
            }

            @Override
            public String exclusiveGroup() {
                return group;
            }

            @Override
            public void render(GuiGraphics graphics, float partialTick) {
                assertSame(fixture.graphics, graphics);
                assertEquals(0.375F, partialTick);
                positions.add(graphics.pose().last().pose().m31());
            }
        };
    }

    private static final class HudFixture {
        final Minecraft minecraft = mock(Minecraft.class);
        final Options options = mock(Options.class);
        final ForgeGui gui = mock(ForgeGui.class);
        final GuiGraphics graphics = mock(GuiGraphics.class);
        final PoseStack pose = new PoseStack();

        HudFixture() throws Exception {
            var optionsField = Minecraft.class.getField("options");
            optionsField.setAccessible(true);
            optionsField.set(minecraft, options);
            minecraft.player = mock(LocalPlayer.class);
            when(gui.getMinecraft()).thenReturn(minecraft);
        }
    }
}
