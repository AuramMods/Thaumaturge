package com.leclowndu93150.thaumaturge.compat.ae2.client;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import com.leclowndu93150.thaumaturge.compat.ae2.ArcaneTerminalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

final class ArcaneTerminalScreen extends MEStorageScreen<ArcaneTerminalMenu> {
    ArcaneTerminalScreen(ArcaneTerminalMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
        var clear = new ActionButton(ActionItems.STASH_TO_PLAYER_INV, button -> menu.clearGrid());
        clear.setHalfSize(true);
        clear.setDisableBackground(true);
        widgets.add("clearToPlayerInv", clear);
        var knowledge = new ActionButton(ActionItems.ENCODE, button -> menu.encode(false));
        knowledge.setHalfSize(true);
        knowledge.setMessage(Component.translatable("gui.thaumaturge.arcane_terminal.encode_knowledge"));
        widgets.add("encodeKnowledge", knowledge);
        var distillation = new ActionButton(ActionItems.CYCLE_PROCESSING_OUTPUT, button -> menu.encode(true));
        distillation.setHalfSize(true);
        distillation.setMessage(Component.translatable("gui.thaumaturge.arcane_terminal.encode_distillation"));
        widgets.add("encodeDistillation", distillation);
    }

    @Override
    public void drawFG(GuiGraphics graphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(graphics, offsetX, offsetY, mouseX, mouseY);
        graphics.drawString(font, menu.craftCost, 32, imageHeight - 98, 0x404040, false);
    }
}
