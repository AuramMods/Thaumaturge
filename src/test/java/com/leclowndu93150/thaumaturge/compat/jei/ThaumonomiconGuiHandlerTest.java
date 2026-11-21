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
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.Minecraft;
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
    private final IClickableIngredientFactory factory = mock(IClickableIngredientFactory.class);

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        IGuiHandlerRegistration registration = mock(IGuiHandlerRegistration.class);
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
        IClickableIngredientFactory.IBuilder<ItemStack> builder = mock(IClickableIngredientFactory.IBuilder.class);
        IClickableIngredient<ItemStack> ingredient = mock(IClickableIngredient.class);
        when(factory.createBuilder(stack)).thenReturn(builder);
        when(builder.buildWithArea(109, 120, 16, 16)).thenReturn(Optional.of(ingredient));

        assertSame(
                ingredient,
                handler.getClickableIngredientUnderMouse(factory, screen, 110, 125)
                        .orElseThrow());
        verify(screen, never()).aspectUnderMouse(anyDouble(), anyDouble());
    }

    @Test
    @SuppressWarnings("unchecked")
    void globalLookupUsesTheActiveResearchScreen() {
        ItemStack stack = new ItemStack(Items.STONE, 5);
        when(screen.itemUnderMouse(110, 125)).thenReturn(new ItemHit(stack, 109, 120));
        IClickableIngredientFactory.IBuilder<ItemStack> builder = mock(IClickableIngredientFactory.IBuilder.class);
        IClickableIngredient<ItemStack> ingredient = mock(IClickableIngredient.class);
        when(factory.createBuilder(stack)).thenReturn(builder);
        when(builder.buildWithArea(109, 120, 16, 16)).thenReturn(Optional.of(ingredient));

        try (MockedStatic<Minecraft> minecraftStatic = mockStatic(Minecraft.class)) {
            Minecraft minecraft = mock(Minecraft.class);
            minecraft.screen = screen;
            minecraftStatic.when(Minecraft::getInstance).thenReturn(minecraft);

            assertSame(
                    ingredient,
                    globalHandler
                            .getClickableIngredientUnderMouse(factory, 110, 125)
                            .orElseThrow());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void aspectLookupUsesJeisAspectTypeAndTheRenderedChipBounds() {
        AspectInstance aspect = new AspectInstance(Holder.direct(mock(IAspect.class)), 12);
        when(screen.aspectUnderMouse(110, 125)).thenReturn(new AspectHit(aspect, 109, 120));
        IClickableIngredientFactory.IBuilder<AspectInstance> builder = mock(IClickableIngredientFactory.IBuilder.class);
        IClickableIngredient<AspectInstance> ingredient = mock(IClickableIngredient.class);
        when(factory.createBuilder(AspectIngredientType.INSTANCE, aspect)).thenReturn(builder);
        when(builder.buildWithArea(109, 120, 16, 16)).thenReturn(Optional.of(ingredient));

        assertSame(
                ingredient,
                handler.getClickableIngredientUnderMouse(factory, screen, 110, 125)
                        .orElseThrow());
    }

    @Test
    void emptyHoverLeavesNormalBookInputAlone() {
        assertTrue(handler.getClickableIngredientUnderMouse(factory, screen, 110, 125)
                .isEmpty());
        verifyNoInteractions(factory);
    }
}
