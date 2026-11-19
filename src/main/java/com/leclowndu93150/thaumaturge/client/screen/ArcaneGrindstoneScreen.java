package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.content.infusion.grindstone.MenuArcaneGrindstone;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ArcaneGrindstoneScreen extends AbstractContainerScreen<MenuArcaneGrindstone> {
    private static final int ERROR_X = 92;
    private static final int ERROR_Y = 31;
    private static final int ERROR_U = 176;
    private static final int ERROR_V = 0;
    private static final int ERROR_W = 28;
    private static final int ERROR_H = 21;

    public ArcaneGrindstoneScreen(MenuArcaneGrindstone menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TTScreenTextures.GRINDSTONE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        boolean hasInput = menu.getSlot(MenuArcaneGrindstone.INPUT_SLOT).hasItem()
                || menu.getSlot(MenuArcaneGrindstone.ADDITIONAL_SLOT).hasItem();
        if (hasInput && !menu.getSlot(MenuArcaneGrindstone.RESULT_SLOT).hasItem()) {
            graphics.blit(TTScreenTextures.GRINDSTONE, leftPos + ERROR_X, topPos + ERROR_Y, ERROR_U, ERROR_V, ERROR_W, ERROR_H);
        }
    }
}
