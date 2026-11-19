package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.client.extensions.TTItemRenderExtensions;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.content.recipe.dust.ItemMultiblockPlacer;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ItemAdvancedAlchemicalFurnace extends ItemMultiblockPlacer {
    private static final ResourceKey<Blueprint> BLUEPRINT =
            ResourceKey.create(Blueprint.REGISTRY_KEY, TTIds.rl("advanced_alchemical_furnace"));

    public ItemAdvancedAlchemicalFurnace(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(TTItemRenderExtensions.ADVANCED_ALCHEMICAL_FURNACE);
    }

    @Override
    protected ResourceKey<Blueprint> blueprint() {
        return BLUEPRINT;
    }
}
