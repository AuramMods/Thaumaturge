package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.leclowndu93150.thaumaturge.TTIds;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FocalSpriteRuntimeTest {
    private static final String FOLDER = "textures/gui/sprites/focal/";

    @AfterEach
    void clearTestSprites() {
        FocalSpriteRenderer.reload(ResourceManager.Empty.INSTANCE);
    }

    @Test
    void shippedButtonRetainsItsCornersAndRepeatsTheEdges() {
        try (var manager = createManager()) {
            reload(manager, base());
            var graphics = mock(GuiGraphics.class);

            FocalDraw.sprite(graphics, FocalSprites.BUTTON_HOVER, 10, 20, 16, 14);

            var button = texture("button_hover");
            verify(graphics).blit(button, 10, 20, 3, 3, 0.0F, 0.0F, 3, 3, 12, 12);
            verify(graphics).blit(button, 13, 20, 6, 3, 3.0F, 0.0F, 6, 3, 12, 12);
            verify(graphics).blit(button, 19, 20, 4, 3, 3.0F, 0.0F, 4, 3, 12, 12);
            verify(graphics).blit(button, 23, 20, 3, 3, 9.0F, 0.0F, 3, 3, 12, 12);
            verify(graphics).blit(button, 23, 31, 3, 3, 9.0F, 9.0F, 3, 3, 12, 12);
        }
    }

    @Test
    void shippedBarsTileAndClipWhilePlainGlyphsUseTheEntireTexture() {
        try (var manager = createManager()) {
            reload(manager, base());
            var graphics = mock(GuiGraphics.class);

            FocalDraw.sprite(graphics, FocalSprites.BAR_FILL_OVER, 2, 4, 37, 7);
            FocalDraw.sprite(graphics, FocalSprites.GLYPH_LEFT, 1, 2, 9, 19);
            FocalDraw.sprite(graphics, FocalSprites.BAR_FILL_OVER, 0, 0, 0, 4);

            var bar = texture("bar_fill_over");
            verify(graphics).blit(bar, 2, 4, 16, 4, 0.0F, 0.0F, 16, 4, 16, 4);
            verify(graphics).blit(bar, 2, 8, 16, 3, 0.0F, 0.0F, 16, 3, 16, 4);
            verify(graphics).blit(bar, 18, 4, 16, 4, 0.0F, 0.0F, 16, 4, 16, 4);
            verify(graphics).blit(bar, 18, 8, 16, 3, 0.0F, 0.0F, 16, 3, 16, 4);
            verify(graphics).blit(bar, 34, 4, 5, 4, 0.0F, 0.0F, 5, 4, 16, 4);
            verify(graphics).blit(bar, 34, 8, 5, 3, 0.0F, 0.0F, 5, 3, 16, 4);
            verify(graphics).blit(texture("glyph_left"), 1, 2, 9, 19, 0.0F, 0.0F, 9, 19, 9, 19);
            verifyNoMoreInteractions(graphics);
        }
    }

    @Test
    void packMetadataOverridesAndRevertsOnNativeReload(@TempDir Path overlay) throws Exception {
        var metadata = overlay.resolve("assets/thaumaturge/" + FOLDER + "button_hover.png.mcmeta");
        Files.createDirectories(metadata.getParent());
        Files.writeString(metadata, """
                {"gui":{"scaling":{"type":"nine_slice","width":12,"height":12,
                  "border":{"left":1,"top":2,"right":4,"bottom":3}}}}
                """);
        try (var manager = createManager()) {
            reload(manager, base(), new PathPackResources("override", overlay, false));
            var overridden = mock(GuiGraphics.class);
            FocalDraw.sprite(overridden, FocalSprites.BUTTON_HOVER, 10, 20, 5, 4);
            verify(overridden).blit(texture("button_hover"), 10, 20, 1, 2, 0.0F, 0.0F, 1, 2, 12, 12);
            verify(overridden).blit(texture("button_hover"), 13, 20, 2, 2, 10.0F, 0.0F, 2, 2, 12, 12);

            reload(manager, base());
            var restored = mock(GuiGraphics.class);
            FocalDraw.sprite(restored, FocalSprites.BUTTON_HOVER, 10, 20, 5, 4);
            verify(restored).blit(texture("button_hover"), 10, 20, 2, 2, 0.0F, 0.0F, 2, 2, 12, 12);
        }
    }

    @Test
    void tintedSpritesRestoreColorIfDrawingFails() {
        var graphics = mock(GuiGraphics.class);
        var glyph = texture("glyph_plus");
        doThrow(new IllegalStateException("fixture draw failed"))
                .when(graphics).blit(glyph, 3, 5, 8, 8, 0.0F, 0.0F, 8, 8, 8, 8);

        assertThrows(IllegalStateException.class,
                () -> FocalDraw.sprite(graphics, FocalSprites.GLYPH_PLUS, 3, 5, 8, 8, 0x80402010));

        var order = inOrder(graphics);
        order.verify(graphics).setColor(64 / 255.0F, 32 / 255.0F, 16 / 255.0F, 128 / 255.0F);
        order.verify(graphics).blit(glyph, 3, 5, 8, 8, 0.0F, 0.0F, 8, 8, 8, 8);
        order.verify(graphics).setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static ReloadableResourceManager createManager() {
        var manager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        var bus = BusBuilder.builder().build();
        bus.register(FocalSpriteRenderer.class);
        bus.post(new RegisterClientReloadListenersEvent(manager));
        return manager;
    }

    private static void reload(ReloadableResourceManager manager, PackResources... packs) {
        manager.createReload(Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE), List.of(packs))
                .done().join();
    }

    private static PathPackResources base() {
        return new PathPackResources("thaumaturge", Path.of(System.getProperty("thaumaturge.testResourceRoot")), false);
    }

    private static ResourceLocation texture(String name) {
        return TTIds.rl(FOLDER + name + ".png");
    }
}
