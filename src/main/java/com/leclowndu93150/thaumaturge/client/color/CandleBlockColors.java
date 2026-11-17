package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class CandleBlockColors {
    private static final int HELD_CANDLE_TINT_INDEX = 1;
    private static final int NO_TINT = 0xFFFFFFFF;

    private CandleBlockColors() {}

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        for (DyeColor dye : DyeColor.values()) {
            int color = 0xFF000000 | dye.getMapColor().col;
            BlockColor source = (state, level, pos, tintIndex) -> color;
            event.register(source, TTBlocks.CANDLES.get(dye).get());
        }
        BlockColor holderTint = (state, level, pos, tintIndex) -> tintIndex == HELD_CANDLE_TINT_INDEX
                ? state.getValue(BlockCandleHolder.CANDLE)
                        .dye()
                        .map(dye -> 0xFF000000 | dye.getMapColor().col)
                        .orElse(NO_TINT)
                : NO_TINT;
        for (RegistryObject<BlockCandleHolder> holder : TTBlocks.CANDLE_HOLDERS.values()) {
            event.register(holderTint, holder.get());
        }
    }
}
