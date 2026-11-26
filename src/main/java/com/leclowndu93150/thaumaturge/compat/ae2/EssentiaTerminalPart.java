package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import com.leclowndu93150.thaumaturge.TTIds;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public final class EssentiaTerminalPart extends AbstractTerminalPart {
    private static final ResourceLocation OFF = TTIds.rl("part/essentia_terminal_off");
    private static final ResourceLocation ON = TTIds.rl("part/essentia_terminal_on");
    private static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, OFF, MODEL_STATUS_OFF);
    private static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, ON, MODEL_STATUS_ON);
    private static final IPartModel MODELS_CHANNEL = new PartModel(MODEL_BASE, ON, MODEL_STATUS_HAS_CHANNEL);

    public EssentiaTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    static List<ResourceLocation> models() {
        return List.of(OFF, ON);
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ThaumaturgeAe2.ESSENTIA_TERMINAL_MENU.get();
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_CHANNEL);
    }
}
