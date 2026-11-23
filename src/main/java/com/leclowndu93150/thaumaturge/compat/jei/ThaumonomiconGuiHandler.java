package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import java.util.Optional;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

final class ThaumonomiconGuiHandler implements IScreenHandler<EntryDetailScreen>, IGlobalGuiHandler {
    private final IIngredientManager ingredients;

    private ThaumonomiconGuiHandler(IIngredientManager ingredients) {
        this.ingredients = ingredients;
    }

    static void register(IGuiHandlerRegistration registration) {
        var handler = new ThaumonomiconGuiHandler(registration.getJeiHelpers().getIngredientManager());
        registration.addGuiScreenHandler(EntryDetailScreen.class, handler);
        registration.addGlobalGuiHandler(handler);
    }

    @Override
    public IGuiProperties apply(EntryDetailScreen screen) {
        // Forge posts ScreenEvent.Opening before initializing the screen's width and height.
        var window = Minecraft.getInstance().getWindow();
        return new Properties(screen.getClass(), window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(double mouseX, double mouseY) {
        if (!(Minecraft.getInstance().screen instanceof EntryDetailScreen screen)) return Optional.empty();
        var item = screen.itemUnderMouse(mouseX, mouseY);
        if (item != null && !item.stack().isEmpty()) {
            return ingredients.createClickableIngredient(item.stack(), new Rect2i(item.x(), item.y(), 16, 16), false)
                    .map(clickable -> clickable);
        }
        var aspect = screen.aspectUnderMouse(mouseX, mouseY);
        if (aspect == null) return Optional.empty();
        return ingredients.createClickableIngredient(AspectIngredientType.INSTANCE, aspect.aspect(),
                new Rect2i(aspect.x(), aspect.y(), 16, 16), false).map(clickable -> clickable);
    }

    private record Properties(Class<? extends Screen> getScreenClass, int getGuiXSize, int getGuiYSize)
            implements IGuiProperties {
        @Override
        public int getGuiLeft() {
            return 0;
        }

        @Override
        public int getGuiTop() {
            return 0;
        }

        @Override
        public int getScreenWidth() {
            return getGuiXSize;
        }

        @Override
        public int getScreenHeight() {
            return getGuiYSize;
        }
    }
}
