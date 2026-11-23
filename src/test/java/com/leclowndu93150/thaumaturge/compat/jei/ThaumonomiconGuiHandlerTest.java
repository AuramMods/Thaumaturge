package com.leclowndu93150.thaumaturge.compat.jei;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget.ItemHit;
import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen;
import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen.AspectHit;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ThaumonomiconGuiHandlerTest {
    private IScreenHandler<EntryDetailScreen> handler;
    private IGlobalGuiHandler globalHandler;
    private final EntryDetailScreen screen = mock(EntryDetailScreen.class);
    private final IIngredientManager ingredients = mock(IIngredientManager.class);

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        IGuiHandlerRegistration registration = mock(IGuiHandlerRegistration.class);
        IJeiHelpers helpers = mock(IJeiHelpers.class);
        when(registration.getJeiHelpers()).thenReturn(helpers);
        when(helpers.getIngredientManager()).thenReturn(ingredients);
        ThaumonomiconGuiHandler.register(registration);
        ArgumentCaptor<IScreenHandler<EntryDetailScreen>> capture =
                ArgumentCaptor.forClass((Class) IScreenHandler.class);
        verify(registration).addGuiScreenHandler(eq(EntryDetailScreen.class), capture.capture());
        handler = capture.getValue();
        ArgumentCaptor<IGlobalGuiHandler> globalCapture = ArgumentCaptor.forClass(IGlobalGuiHandler.class);
        verify(registration).addGlobalGuiHandler(globalCapture.capture());
        globalHandler = globalCapture.getValue();
        assertSame(handler, globalHandler);
    }

    @ParameterizedTest
    @CsvSource({"0, 0, 640, 360", "640, 360, 427, 240"})
    void guiPropertiesReserveTheScaledWindowBeforeScreenInitialization(
            int screenWidth, int screenHeight, int windowWidth, int windowHeight) {
        screen.width = screenWidth;
        screen.height = screenHeight;
        var window = mock(Window.class);
        when(window.getGuiScaledWidth()).thenReturn(windowWidth);
        when(window.getGuiScaledHeight()).thenReturn(windowHeight);
        var minecraft = mock(Minecraft.class);
        when(minecraft.getWindow()).thenReturn(window);

        try (MockedStatic<Minecraft> minecraftStatic = mockStatic(Minecraft.class)) {
            minecraftStatic.when(Minecraft::getInstance).thenReturn(minecraft);

            var properties = handler.apply(screen);

            assertNotNull(properties); // JEI skips all keyboard lookup when these are null.
            assertEquals(EntryDetailScreen.class, properties.getScreenClass());
            assertEquals(0, properties.getGuiLeft());
            assertEquals(0, properties.getGuiTop());
            assertEquals(windowWidth, properties.getGuiXSize());
            assertEquals(windowHeight, properties.getGuiYSize());
            assertEquals(windowWidth, properties.getScreenWidth());
            assertEquals(windowHeight, properties.getScreenHeight());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void itemLookupUsesTheDisplayedStackAndItsActualBounds() {
        ItemStack stack = new ItemStack(Items.STONE, 5);
        when(screen.itemUnderMouse(110, 125)).thenReturn(new ItemHit(stack, 109, 120));
        IClickableIngredient<ItemStack> ingredient = mock(IClickableIngredient.class);
        when(ingredients.createClickableIngredient(eq(stack), any(Rect2i.class), eq(false)))
                .thenReturn(Optional.of(ingredient));

        assertSame(ingredient, hovered(screen).orElseThrow());
        var area = ArgumentCaptor.forClass(Rect2i.class);
        verify(ingredients).createClickableIngredient(eq(stack), area.capture(), eq(false));
        assertArea(area.getValue());
        verify(screen, never()).aspectUnderMouse(anyDouble(), anyDouble());
    }

    @Test
    @SuppressWarnings("unchecked")
    void aspectLookupUsesJeisAspectTypeAndTheRenderedChipBounds() {
        AspectInstance aspect = new AspectInstance(Holder.direct(mock(IAspect.class)), 12);
        when(screen.aspectUnderMouse(110, 125)).thenReturn(new AspectHit(aspect, 109, 120));
        IClickableIngredient<AspectInstance> ingredient = mock(IClickableIngredient.class);
        when(ingredients.createClickableIngredient(eq(AspectIngredientType.INSTANCE), eq(aspect), any(Rect2i.class),
                eq(false))).thenReturn(Optional.of(ingredient));

        assertSame(ingredient, hovered(screen).orElseThrow());
        var area = ArgumentCaptor.forClass(Rect2i.class);
        verify(ingredients).createClickableIngredient(eq(AspectIngredientType.INSTANCE), eq(aspect), area.capture(),
                eq(false));
        assertArea(area.getValue());
    }

    @Test
    void emptyHoverLeavesNormalBookInputAlone() {
        assertTrue(hovered(screen).isEmpty());
        assertTrue(hovered(null).isEmpty());
        verifyNoInteractions(ingredients);
    }

    private Optional<IClickableIngredient<?>> hovered(EntryDetailScreen current) {
        try (MockedStatic<Minecraft> minecraftStatic = mockStatic(Minecraft.class)) {
            Minecraft minecraft = mock(Minecraft.class);
            minecraft.screen = current;
            minecraftStatic.when(Minecraft::getInstance).thenReturn(minecraft);
            return globalHandler.getClickableIngredientUnderMouse(110, 125);
        }
    }

    private static void assertArea(Rect2i area) {
        assertEquals(109, area.getX());
        assertEquals(120, area.getY());
        assertEquals(16, area.getWidth());
        assertEquals(16, area.getHeight());
    }
}
